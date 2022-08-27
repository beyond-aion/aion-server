package com.aionemu.gameserver.services;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.aionemu.gameserver.configs.main.AutoGroupConfig;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.ChatType;
import com.aionemu.gameserver.model.autogroup.*;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.team.alliance.PlayerAllianceService;
import com.aionemu.gameserver.model.team.group.PlayerGroup;
import com.aionemu.gameserver.model.team.group.PlayerGroupService;
import com.aionemu.gameserver.model.templates.InstanceCooltime;
import com.aionemu.gameserver.network.aion.serverpackets.SM_AUTO_GROUP;
import com.aionemu.gameserver.network.aion.serverpackets.SM_MESSAGE;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.instance.InstanceService;
import com.aionemu.gameserver.services.instance.PvPArenaService;
import com.aionemu.gameserver.services.instance.periodic.*;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.world.World;
import com.aionemu.gameserver.world.WorldMapInstance;

/**
 * @author xTz
 */
public class AutoGroupService {

	private Map<Integer, LookingForParty> searchers = new ConcurrentHashMap<>();
	private Map<Integer, AutoInstance> autoInstances = new ConcurrentHashMap<>();
	private List<Integer> penalties = Collections.synchronizedList(new ArrayList<>());
	private Lock lock = new ReentrantLock();

	private AutoGroupService() {
	}

	public void startLooking(Player player, int instanceMaskId, EntryRequestType ert) {
		AutoGroupType agt = AutoGroupType.getAGTByMaskId(instanceMaskId);
		if (agt == null) {
			return;
		}
		if (!canEnter(player, ert, agt)) {
			return;
		}
		int obj = player.getObjectId();
		LookingForParty lfp = searchers.get(obj);
		if (penalties.contains(obj)) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_CANT_INSTANCE_ALREADY_REGISTERED(agt.getInstanceMapId()));
			return;
		}
		if (lfp == null) {
			searchers.putIfAbsent(obj, new LookingForParty(player, instanceMaskId, ert));
		} else if (lfp.hasPenalty() || lfp.isRegistredInstance(instanceMaskId)) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_CANT_INSTANCE_ALREADY_REGISTERED(agt.getInstanceMapId()));
			return;
		} else {
			lfp.addInstanceMaskId(instanceMaskId, ert);
		}

		if (ert.isGroupEntry()) {
			for (Player member : player.getPlayerGroup().getOnlineMembers()) {
				if (agt.isIconInvite()) {
					PacketSendUtility.sendPacket(member, new SM_AUTO_GROUP(instanceMaskId, 6, true));
				}
				PacketSendUtility.sendPacket(member, SM_SYSTEM_MESSAGE.STR_MSG_INSTANCE_REGISTER_SUCCESS());
				PacketSendUtility.sendPacket(member, new SM_AUTO_GROUP(instanceMaskId, 1, ert.getId(), player.getName()));
			}
		} else {
			if (agt.isIconInvite()) {
				PacketSendUtility.sendPacket(player, new SM_AUTO_GROUP(instanceMaskId, 6, true));
			}
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_INSTANCE_REGISTER_SUCCESS());
			PacketSendUtility.sendPacket(player, new SM_AUTO_GROUP(instanceMaskId, 1, ert.getId(), player.getName()));
		}
		if (agt.isIconInvite() && AutoGroupConfig.ANNOUNCE_BATTLEGROUND_REGISTRATIONS && searchers.values().stream().filter(s -> s.getPlayer() != null && s.getPlayer().getRace() == player.getRace() && s.getSearchInstance(instanceMaskId) != null).count() == 1)
			PacketSendUtility.broadcastToWorld(new SM_MESSAGE(0, null, player.getRace().getL10n() + " have registered for " + agt.getL10n() + ".", ChatType.BRIGHT_YELLOW_CENTER), p -> p.getRace() != player.getRace() && agt.hasLevelPermit(p.getLevel()));
		startSort(ert, instanceMaskId, true);
	}

	public synchronized void pressEnter(Player player, int instanceMaskId) {
		AutoInstance instance = getAutoInstance(player, instanceMaskId);
		if (instance == null || instance.players.get(player.getObjectId()).isPressedEnter()) {
			return;
		}
		if (player.isInGroup()) {
			PlayerGroupService.removePlayer(player);
		}
		if (player.isInAlliance()) {
			PlayerAllianceService.removePlayer(player);
		}
		instance.onPressEnter(player);
		PacketSendUtility.sendPacket(player, new SM_AUTO_GROUP(instanceMaskId, 5));
	}

	public void onEnterInstance(Player player) {
		if (player.isInInstance()) {
			int obj = player.getObjectId();
			AutoInstance autoInstance = autoInstances.get(player.getInstanceId());
			if (autoInstance != null && autoInstance.players.containsKey(obj)) {
				autoInstance.onEnterInstance(player);
			}
		}
	}

	public void unregisterLooking(Player player, int instanceMaskId) {
		int obj = player.getObjectId();
		LookingForParty lfp = searchers.get(obj);
		SearchInstance si;
		if (lfp != null) {
			lfp.setPenaltyTime();
			si = lfp.getSearchInstance(instanceMaskId);
			if (si != null) {
				if (lfp.unregisterInstance(instanceMaskId) == 0) {
					searchers.remove(obj);
					startPenalty(obj);
				}
				unregisterSearchInstance(player, si);
			}
		}
	}

	public void cancelEnter(Player player, int instanceMaskId) {
		AutoInstance autoInstance = getAutoInstance(player, instanceMaskId);
		if (autoInstance != null) {
			int obj = player.getObjectId();
			if (!autoInstance.players.get(obj).isInInstance()) {
				autoInstance.unregister(player);
				if (!searchers.containsKey(obj)) {
					startPenalty(obj);
				}
				if (autoInstance.agt.hasRegisterQuick()) {
					startSort(EntryRequestType.QUICK_GROUP_ENTRY, instanceMaskId, false);
				}
				if (autoInstance.players.isEmpty()) {
					WorldMapInstance instance = autoInstance.instance;
					autoInstance = autoInstances.remove(instance.getInstanceId());
					InstanceService.destroyInstance(instance);
					autoInstance.clear();
				}
			}
			if (autoInstance.agt.isIronWarFront() && IronWallFrontService.getInstance().isRegisterAvailable()) {
				PacketSendUtility.sendPacket(player, new SM_AUTO_GROUP(instanceMaskId, 6));
			} else if (autoInstance.agt.isDredgion() && DredgionService.getInstance().isRegisterAvailable()) {
				PacketSendUtility.sendPacket(player, new SM_AUTO_GROUP(instanceMaskId, 6));
			} else if (autoInstance.agt.isKamar() && KamarBattlefieldService.getInstance().isRegisterAvailable()) {
				PacketSendUtility.sendPacket(player, new SM_AUTO_GROUP(instanceMaskId, 6));
			} else if (autoInstance.agt.isEngulfedOB() && EngulfedOphidianBridgeService.getInstance().isRegisterAvailable()) {
				PacketSendUtility.sendPacket(player, new SM_AUTO_GROUP(instanceMaskId, 6));
			} else if (autoInstance.agt.isIdgelDome() && IdgelDomeService.getInstance().isRegisterAvailable()) {
				PacketSendUtility.sendPacket(player, new SM_AUTO_GROUP(instanceMaskId, 6));
			}
			PacketSendUtility.sendPacket(player, new SM_AUTO_GROUP(instanceMaskId, 2));
		}
	}

	public void onPlayerLogin(Player player) {
		for (int maskId : DredgionService.getInstance().getMaskIds()) {
			boolean closed = true;
			if (DredgionService.getInstance().getInstanceMaskId(player) == maskId) {
				closed = !DredgionService.getInstance().isEnterAvailable(player);
			}
			PacketSendUtility.sendPacket(player, new SM_AUTO_GROUP(maskId, SM_AUTO_GROUP.wnd_EntryIcon, closed));
		}
		for (int maskId : KamarBattlefieldService.getInstance().getMaskIds())
			PacketSendUtility.sendPacket(player,
				new SM_AUTO_GROUP(maskId, SM_AUTO_GROUP.wnd_EntryIcon, !KamarBattlefieldService.getInstance().isEnterAvailable(player)));
		for (int maskId : EngulfedOphidianBridgeService.getInstance().getMaskIds())
			PacketSendUtility.sendPacket(player,
				new SM_AUTO_GROUP(maskId, SM_AUTO_GROUP.wnd_EntryIcon, !EngulfedOphidianBridgeService.getInstance().isEnterAvailable(player)));
		for (int maskId : IronWallFrontService.getInstance().getMaskIds())
			PacketSendUtility.sendPacket(player,
				new SM_AUTO_GROUP(maskId, SM_AUTO_GROUP.wnd_EntryIcon, !IronWallFrontService.getInstance().isEnterAvailable(player)));
		for (int maskId : IdgelDomeService.getInstance().getMaskIds())
			PacketSendUtility.sendPacket(player,
				new SM_AUTO_GROUP(maskId, SM_AUTO_GROUP.wnd_EntryIcon, !IdgelDomeService.getInstance().isEnterAvailable(player)));
		int obj = player.getObjectId();
		LookingForParty lfp = searchers.get(obj);
		if (lfp != null) {
			for (SearchInstance searchInstance : lfp.getSearchInstances()) {
				if (searchInstance.getEntryRequestType().isGroupEntry() && !player.isInGroup()) {
					int instanceMaskId = searchInstance.getInstanceMaskId();
					lfp.unregisterInstance(instanceMaskId);
					if (searchInstance.isIronWallFront() && IronWallFrontService.getInstance().isRegisterAvailable()) {
						PacketSendUtility.sendPacket(player, new SM_AUTO_GROUP(instanceMaskId, SM_AUTO_GROUP.wnd_EntryIcon));
					} else if (searchInstance.isDredgion() && DredgionService.getInstance().isRegisterAvailable()) {
						PacketSendUtility.sendPacket(player, new SM_AUTO_GROUP(instanceMaskId, SM_AUTO_GROUP.wnd_EntryIcon));
					} else if (searchInstance.isKamar() && KamarBattlefieldService.getInstance().isRegisterAvailable()) {
						PacketSendUtility.sendPacket(player, new SM_AUTO_GROUP(instanceMaskId, SM_AUTO_GROUP.wnd_EntryIcon));
					} else if (searchInstance.isEngulfedOB() && EngulfedOphidianBridgeService.getInstance().isRegisterAvailable()) {
						PacketSendUtility.sendPacket(player, new SM_AUTO_GROUP(instanceMaskId, SM_AUTO_GROUP.wnd_EntryIcon));
					} else if (searchInstance.isIdgelDome() && IdgelDomeService.getInstance().isRegisterAvailable()) {
						PacketSendUtility.sendPacket(player, new SM_AUTO_GROUP(instanceMaskId, SM_AUTO_GROUP.wnd_EntryIcon));
					}
					PacketSendUtility.sendPacket(player, new SM_AUTO_GROUP(instanceMaskId, 2));
					continue;
				}
				PacketSendUtility.sendPacket(player, new SM_AUTO_GROUP(searchInstance.getInstanceMaskId(), 8,
					searchInstance.getRemainingTime() + searchInstance.getEntryRequestType().getId(), player.getName()));
				if (searchInstance.isDredgion()) {
					PacketSendUtility.sendPacket(player, new SM_AUTO_GROUP(searchInstance.getInstanceMaskId(), SM_AUTO_GROUP.wnd_EntryIcon, true));
				}
			}
			if (lfp.getSearchInstances().isEmpty()) {
				searchers.remove(obj);
				return;
			}
			lfp.setPlayer(player);
			for (SearchInstance si : lfp.getSearchInstances()) {
				startSort(si.getEntryRequestType(), si.getInstanceMaskId(), true);
			}
		}
	}

	public void onEnterWorld(Player player) {
		for (int maskId : DredgionService.getInstance().getMaskIds()) {
			boolean closed = true;
			if (DredgionService.getInstance().getInstanceMaskId(player) == maskId) {
				closed = !DredgionService.getInstance().isEnterAvailable(player);
			}
			PacketSendUtility.sendPacket(player, new SM_AUTO_GROUP(maskId, SM_AUTO_GROUP.wnd_EntryIcon, closed));
		}
		for (int maskId : KamarBattlefieldService.getInstance().getMaskIds())
			PacketSendUtility.sendPacket(player,
				new SM_AUTO_GROUP(maskId, SM_AUTO_GROUP.wnd_EntryIcon, !KamarBattlefieldService.getInstance().isEnterAvailable(player)));
		for (int maskId : EngulfedOphidianBridgeService.getInstance().getMaskIds())
			PacketSendUtility.sendPacket(player,
				new SM_AUTO_GROUP(maskId, SM_AUTO_GROUP.wnd_EntryIcon, !EngulfedOphidianBridgeService.getInstance().isEnterAvailable(player)));
		for (int maskId : IronWallFrontService.getInstance().getMaskIds())
			PacketSendUtility.sendPacket(player,
				new SM_AUTO_GROUP(maskId, SM_AUTO_GROUP.wnd_EntryIcon, !IronWallFrontService.getInstance().isEnterAvailable(player)));
		for (int maskId : IdgelDomeService.getInstance().getMaskIds())
			PacketSendUtility.sendPacket(player,
				new SM_AUTO_GROUP(maskId, SM_AUTO_GROUP.wnd_EntryIcon, !IdgelDomeService.getInstance().isEnterAvailable(player)));
	}

	public void onPlayerLogOut(Player player) {
		int obj = player.getObjectId();
		int instanceId = player.getInstanceId();
		LookingForParty lfp = searchers.get(obj);
		if (lfp != null) {
			lfp.setPlayer(null);
			if (lfp.isOnStartEnterTask()) {
				for (AutoInstance autoInstance : autoInstances.values()) {
					if (autoInstance.players.containsKey(obj) && !autoInstance.players.get(obj).isInInstance()) {
						cancelEnter(player, autoInstance.agt.getInstanceMaskId());
					}
				}
			}
		}

		if (player.isInInstance()) {
			AutoInstance autoInstance = autoInstances.get(instanceId);
			if (autoInstance != null && autoInstance.players.containsKey(obj)) {
				WorldMapInstance instance = autoInstance.instance;
				if (instance != null) {
					autoInstance.players.get(obj).setOnline(false);
					if (autoInstance.players.values().stream().noneMatch(AGPlayer::isOnline)) {
						autoInstance = autoInstances.remove(instanceId);
						InstanceService.destroyInstance(instance);
						autoInstance.clear();
					}
				}
			}
		}
	}

	public void onLeaveInstance(Player player) {
		if (player.isInInstance()) {
			int obj = player.getObjectId();
			int instanceId = player.getInstanceId();
			AutoInstance autoInstance = autoInstances.get(instanceId);
			if (autoInstance != null && autoInstance.players.containsKey(obj)) {
				autoInstance.onLeaveInstance(player);
				if (autoInstance.players.values().stream().noneMatch(AGPlayer::isOnline)) {
					WorldMapInstance instance = autoInstance.instance;
					autoInstances.remove(instanceId);
					if (instance != null)
						InstanceService.destroyInstance(instance);
					autoInstance.clear();
				} else if (autoInstance.agt.hasRegisterQuick()) {
					startSort(EntryRequestType.QUICK_GROUP_ENTRY, autoInstance.agt.getInstanceMaskId(), false);
				}
			}
		}
	}

	private void startSort(EntryRequestType ert, Integer instanceMaskId, boolean checkNewGroup) {
		lock.lock();
		try {
			Collection<Player> players = new HashSet<>();
			if (ert.isQuickGroupEntry()) {
				for (LookingForParty lfp : searchers.values()) {
					if (lfp.getPlayer() == null || lfp.isOnStartEnterTask()) {
						continue;
					}
					for (AutoInstance autoInstance : autoInstances.values()) {
						int searchMaskId = autoInstance.agt.getInstanceMaskId();
						SearchInstance searchInstance = lfp.getSearchInstance(searchMaskId);
						if (searchInstance != null && searchInstance.getEntryRequestType().isQuickGroupEntry()) {
							Player owner = lfp.getPlayer();
							if (autoInstance.addPlayer(owner, searchInstance).isAdded()) {
								lfp.setStartEnterTime();
								if (lfp.unregisterInstance(searchMaskId) == 0) {
									players.add(owner);
								}
								PacketSendUtility.sendPacket(lfp.getPlayer(), new SM_AUTO_GROUP(searchMaskId, 4));
							}
						}
					}
				}
				for (Player p : players) {
					searchers.remove(p.getObjectId());
				}
				players.clear();
			}
			if (checkNewGroup) {
				AutoGroupType agt = AutoGroupType.getAGTByMaskId(instanceMaskId);
				AutoInstance autoInstance = agt.getAutoInstance();
				boolean canCreate = false;
				Iterator<LookingForParty> iter = searchers.values().iterator();
				LookingForParty lfp;
				while (iter.hasNext()) {
					lfp = iter.next();
					if (lfp.getPlayer() == null || lfp.isOnStartEnterTask()) {
						continue;
					}
					SearchInstance searchInstance = lfp.getSearchInstance(instanceMaskId);
					if (searchInstance != null) {
						if (searchInstance.getEntryRequestType().isGroupEntry()) {
							if (!lfp.getPlayer().isInGroup()) {
								if (lfp.unregisterInstance(instanceMaskId) == 0) {
									iter.remove();
								}
								continue;
							}
						}
						AGQuestion question = autoInstance.addPlayer(lfp.getPlayer(), searchInstance);
						if (!question.isFailed()) {
							if (searchInstance.getEntryRequestType().isGroupEntry()) {
								for (Player member : lfp.getPlayer().getPlayerGroup().getOnlineMembers()) {
									if (searchInstance.getMembers().contains(member.getObjectId())) {
										players.add(member);
									}
								}
							} else {
								players.add(lfp.getPlayer());
							}
						}
						if (question.isReady()) {
							canCreate = true;
							break;
						}
					}
				}
				if (canCreate) {
					WorldMapInstance instance = InstanceService.getNextAvailableInstance(agt.getInstanceMapId(), 0, agt.getDifficultId(), null, autoInstance.getMaxPlayers(), false);
					autoInstance.onInstanceCreate(instance);
					autoInstances.put(instance.getInstanceId(), autoInstance);
					for (Player player : players) {
						int obj = player.getObjectId();
						lfp = searchers.get(obj);
						if (lfp != null) {
							lfp.setStartEnterTime();
							if (lfp.unregisterInstance(autoInstance.agt.getInstanceMaskId()) == 0) {
								searchers.remove(obj);
							}
						}
						PacketSendUtility.sendPacket(player, new SM_AUTO_GROUP(instanceMaskId, 4));
					}
				} else {
					autoInstance.clear();
				}
				players.clear();
			}
		} finally {
			lock.unlock();
		}
	}

	private boolean canEnter(Player player, EntryRequestType ert, AutoGroupType agt) {
		int mapId = agt.getInstanceMapId();
		int instanceMaskId = agt.getInstanceMaskId();
		if (!agt.hasLevelPermit(player.getLevel())) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_CANT_INSTANCE_ENTER_LEVEL());
			return false;
		}
		if (agt.isIronWarFront() && !IronWallFrontService.getInstance().isRegisterAvailable()) {
			return false;
		} else if (agt.isKamar() && !KamarBattlefieldService.getInstance().isRegisterAvailable()) {
			return false;
		} else if (agt.isEngulfedOB() && !EngulfedOphidianBridgeService.getInstance().isRegisterAvailable()) {
			return false;
		} else if (agt.isIdgelDome() && !IdgelDomeService.getInstance().isRegisterAvailable()) {
			return false;
		} else if (agt.isDredgion() && !DredgionService.getInstance().isRegisterAvailable()) {
			return false;
		} else if ((agt.isPvPFFAArena() || agt.isPvPSoloArena() || agt.isHarmonyArena() || agt.isGloryArena())
			&& !PvPArenaService.isPvPArenaAvailable(player, agt)) {
			return false;
		} else if (hasCoolDown(player, mapId)) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_CANNOT_MAKE_INSTANCE_COOL_TIME());
			return false;
		}
		switch (ert) {
			case NEW_GROUP_ENTRY:
				if (!agt.hasRegisterNew()) {
					return false;
				}
				break;
			case QUICK_GROUP_ENTRY:
				if (!agt.hasRegisterQuick()) {
					return false;
				}
				break;
			case GROUP_ENTRY:
				if (!agt.hasRegisterGroup()) {
					return false;
				}
				PlayerGroup group = player.getPlayerGroup();
				if (group == null || !group.isLeader(player)) {
					PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_CANT_INSTANCE_NOT_LEADER());
					return false;
				}
				if (agt.isHarmonyArena() || agt.isTrainigHarmonyArena()) {
					if (group.getOnlineMembers().size() > 3) {
						PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_CANT_INSTANCE_TOO_MANY_MEMBERS(3, mapId));
						return false;
					}
				}
				for (Player member : group.getMembers()) {
					if (group.getLeaderObject().equals(member)) {
						continue;
					}
					LookingForParty lfp = searchers.get(member.getObjectId());
					if (lfp != null && lfp.isRegistredInstance(instanceMaskId)) {
						PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_CANT_INSTANCE_ENTER_MEMBER(member.getName()));
						return false;
					}
					if (agt.isHarmonyArena() && !PvPArenaService.checkItem(member, agt)) {
						PacketSendUtility.sendPacket(member, SM_SYSTEM_MESSAGE.STR_MSG_INSTANCE_CANT_ENTER_WITHOUT_ITEM());
						PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_CANT_INSTANCE_ENTER_MEMBER(member.getName()));
						return false;
					}
					if (agt.isIronWarFront() && IronWallFrontService.getInstance().hasCooldown(member)) {
						PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_CANT_INSTANCE_ENTER_MEMBER(member.getName()));
						return false;
					} else if (agt.isEngulfedOB() && EngulfedOphidianBridgeService.getInstance().hasCooldown(member)) {
						PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_CANT_INSTANCE_ENTER_MEMBER(member.getName()));
						return false;
					} else if (agt.isIdgelDome() && IdgelDomeService.getInstance().hasCooldown(member)) {
						PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_CANT_INSTANCE_ENTER_MEMBER(member.getName()));
						return false;
					} else if (agt.isKamar() && KamarBattlefieldService.getInstance().hasCooldown(member)) {
						PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_CANT_INSTANCE_ENTER_MEMBER(member.getName()));
						return false;
					} else if (agt.isDredgion() && DredgionService.getInstance().hasCooldown(member)) {
						PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_CANT_INSTANCE_ENTER_MEMBER(member.getName()));
						return false;
					} else if (hasCoolDown(member, mapId)) {
						PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_CANT_INSTANCE_ENTER_MEMBER(member.getName()));
						return false;
					}
					if (!agt.hasLevelPermit(member.getLevel())) {
						PacketSendUtility.sendPacket(member, SM_SYSTEM_MESSAGE.STR_MSG_CANT_INSTANCE_ENTER_LEVEL());
						PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_CANT_INSTANCE_ENTER_MEMBER(member.getName()));
						return false;
					}
				}
				break;
		}
		return true;
	}

	private AutoInstance getAutoInstance(Player player, int instanceMaskId) {
		for (AutoInstance autoInstance : autoInstances.values()) {
			if (autoInstance.agt.getInstanceMaskId() == instanceMaskId && autoInstance.players.containsKey(player.getObjectId())) {
				return autoInstance;
			}
		}
		return null;
	}

	private boolean hasCoolDown(Player player, int worldId) {
		int instanceCooldownRate = InstanceService.getInstanceRate(player, worldId);
		int useDelay = 0;
		int instanceCooldown = 0;
		InstanceCooltime clt = DataManager.INSTANCE_COOLTIME_DATA.getInstanceCooltimeByWorldId(worldId);
		if (clt != null) {
			instanceCooldown = clt.getEntCoolTime();
		}
		if (instanceCooldownRate > 0) {
			useDelay = instanceCooldown / instanceCooldownRate;
		}
		return player.getPortalCooldownList().isPortalUseDisabled(worldId) && useDelay > 0;
	}

	private void startPenalty(final Integer obj) {
		if (penalties.contains(obj))
			return;
		penalties.add(obj);
		ThreadPoolManager.getInstance().schedule(() -> penalties.remove(obj), 10000);
	}

	public void unregisterInstance(int instanceMaskId) {
		for (LookingForParty lfp : searchers.values()) {
			if (lfp.isRegistredInstance(instanceMaskId)) {
				if (lfp.getPlayer() != null) {
					unregisterLooking(lfp.getPlayer(), instanceMaskId);
				} else {
					unregisterSearchInstance(null, lfp.getSearchInstance(instanceMaskId));
					if (lfp.unregisterInstance(instanceMaskId) == 0) {
						searchers.values().remove(lfp);
					}
				}
			}
		}
	}

	private void unregisterSearchInstance(Player player, SearchInstance si) {
		int instanceMaskId = si.getInstanceMaskId();
		if (si.getEntryRequestType().isGroupEntry() && si.getMembers() != null) {
			for (Integer obj : si.getMembers()) {
				Player member = World.getInstance().getPlayer(obj);
				if (member != null) {
					if (si.isDredgion() && DredgionService.getInstance().isRegisterAvailable()) {
						PacketSendUtility.sendPacket(member, new SM_AUTO_GROUP(instanceMaskId, 6));
					}
					PacketSendUtility.sendPacket(member, new SM_AUTO_GROUP(instanceMaskId, 2));
				}
			}
		}
		if (player != null) {
			if (si.isDredgion() && DredgionService.getInstance().isRegisterAvailable()) {
				PacketSendUtility.sendPacket(player, new SM_AUTO_GROUP(instanceMaskId, 6));
			}
			PacketSendUtility.sendPacket(player, new SM_AUTO_GROUP(instanceMaskId, 2));
		}
	}

	public void unregisterAndDestroyInstance(int instanceId) {
		AutoInstance autoInstance = autoInstances.remove(instanceId);
		if (autoInstance != null) {
			WorldMapInstance instance = autoInstance.instance;
			if (instance != null)
				InstanceService.destroyInstance(instance);
			autoInstance.clear();
		}
	}

	public boolean isAutoInstance(int instanceId) {
		return autoInstances.containsKey(instanceId);
	}

	public static AutoGroupService getInstance() {
		return NewSingletonHolder.INSTANCE;
	}

	private static class NewSingletonHolder {

		private static final AutoGroupService INSTANCE = new AutoGroupService();
	}

}
