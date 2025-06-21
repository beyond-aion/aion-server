package com.aionemu.gameserver.services;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import com.aionemu.gameserver.configs.main.AutoGroupConfig;
import com.aionemu.gameserver.model.ChatType;
import com.aionemu.gameserver.model.autogroup.*;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.team.alliance.PlayerAllianceService;
import com.aionemu.gameserver.model.team.group.PlayerGroupService;
import com.aionemu.gameserver.network.aion.serverpackets.SM_AUTO_GROUP;
import com.aionemu.gameserver.network.aion.serverpackets.SM_MESSAGE;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.autogroup.AutoGroupUtility;
import com.aionemu.gameserver.services.instance.InstanceService;
import com.aionemu.gameserver.services.instance.PeriodicInstanceManager;
import com.aionemu.gameserver.services.instance.PvPArenaService;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.world.WorldMapInstance;

/**
 * @author xTz, Estrayl
 */
public class AutoGroupService {

	private final Map<WorldMapInstance, AutoInstance> autoInstances = new ConcurrentHashMap<>();
	private final Map<Integer, List<LookingForParty>> lookingParties = new ConcurrentHashMap<>();
	private final Set<Integer> penalties = ConcurrentHashMap.newKeySet();

	private AutoGroupService() {
	}

	public void startLooking(Player player, int maskId, EntryRequestType ert) {
		AutoGroupType agt = AutoGroupType.getAGTByMaskId(maskId);
		if (agt == null || !canRegister(player, ert, agt))
			return;
		List<LookingForParty> lfps = lookingParties.computeIfAbsent(maskId, k -> new ArrayList<>());
		LookingForParty lfp;
		synchronized (lfps) {
			lfp = getSearchEntry(player.getObjectId(), lfps);
			if (lfp != null) {
				PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_CANT_INSTANCE_ALREADY_REGISTERED(agt.getTemplate().getInstanceMapId()));
				return;
			}
			lfp = new LookingForParty(player, ert, maskId);
			lfps.add(lfp);

			AutoGroupUtility.sendSuccessfulRegistration(lfp, player.getName(), agt, maskId);
			if (AutoGroupConfig.ANNOUNCE_BATTLEGROUND_REGISTRATIONS && agt.isPeriodicInstance() && ert == EntryRequestType.GROUP_ENTRY
				&& lfps.stream().filter(s -> s.getRace() == player.getRace()).count() == 1) {
				PacketSendUtility.broadcastToWorld(
					new SM_MESSAGE(0, null, player.getRace().getL10n() + " have registered for " + agt.getL10n() + ".", ChatType.BRIGHT_YELLOW_CENTER),
					p -> p.getRace() != player.getRace() && agt.isInLvlRange(p.getLevel()));
			}

			if (!checkInstancesForOpenQuickEntries(lfp, maskId))
				checkQueueForNewMatches(maskId);
		}
	}

	private void checkQueueForNewMatches(int maskId) {
		List<LookingForParty> queuedParties = lookingParties.get(maskId);
		if (queuedParties == null || queuedParties.isEmpty())
			return;
		AutoGroupType agt = AutoGroupType.getAGTByMaskId(maskId);
		if (agt == null)
			return;
		synchronized (queuedParties) {
			queuedParties.sort(null);

			for (int i = 0; i < queuedParties.size(); i++) {
				AutoInstance autoInstance = agt.createAutoInstance();
				LookingForParty lfp = queuedParties.get(i);
				AGQuestion question = autoInstance.addLookingForParty(lfp);
				if (question == AGQuestion.FAILED)
					continue;
				List<LookingForParty> filteredParties = new ArrayList<>();
				filteredParties.add(lfp);
				if (question != AGQuestion.READY) {
					for (int j = i + 1; j < queuedParties.size(); j++) {
						lfp = queuedParties.get(j);
						question = autoInstance.addLookingForParty(lfp);
						if (question != AGQuestion.FAILED) {
							filteredParties.add(lfp);
							if (question == AGQuestion.READY)
								break;
						}
					}
				}
				if (question == AGQuestion.READY) {
					createNewInstance(autoInstance, agt, filteredParties, maskId);
					break;
				}
			}
		}
	}

	private void createNewInstance(AutoInstance autoInstance, AutoGroupType agt, List<LookingForParty> filteredParties, int maskId) {
		WorldMapInstance instance = InstanceService.getNextAvailableInstance(agt.getTemplate().getInstanceMapId(), 0, agt.getDifficultId(), null,
			autoInstance.getMaxPlayers(), false);
		autoInstance.onInstanceCreate(instance);
		autoInstances.put(instance, autoInstance);
		for (LookingForParty lfp : filteredParties) {
			removeSearchEntry(lfp);
			lfp.setStartEnterTime();
			lfp.getMembers().keySet().forEach(id -> {
				searchAndRemoveAdditionalRegistrations(id);
				AutoGroupUtility.sendWindowToPlayerIfOnline(id, maskId, 4);
			});
		}
	}

	private boolean checkInstancesForOpenQuickEntries(LookingForParty lfp, int maskId) {
		if (lfp.getEntryRequestType() != EntryRequestType.QUICK_GROUP_ENTRY || lfp.isOnStartEnterTask())
			return false;
		for (AutoInstance autoInstance : autoInstances.values()) {
			if (autoInstance.getAutoGroupType().getTemplate().getMaskId() == maskId && autoInstance.addLookingForParty(lfp) == AGQuestion.ADDED) {
				removeSearchEntry(lfp);
				lfp.setStartEnterTime();
				AutoGroupUtility.sendWindowToPlayerIfOnline(lfp.getLeaderObjId(), maskId, 4);
				searchAndRemoveAdditionalRegistrations(lfp.getLeaderObjId());
				return true;
			}
		}
		return false;
	}

	private void checkQueueForQuickEntries(AutoInstance autoInstance) {
		int maskId = autoInstance.getAutoGroupType().getTemplate().getMaskId();
		List<LookingForParty> parties = lookingParties.get(maskId);
		if (parties == null || parties.isEmpty())
			return;
		synchronized (parties) {
			for (LookingForParty lfp : parties) {
				if (lfp.getEntryRequestType() == EntryRequestType.QUICK_GROUP_ENTRY && !lfp.isOnStartEnterTask()
					&& autoInstance.addLookingForParty(lfp) == AGQuestion.ADDED) {
					removeSearchEntry(lfp);
					lfp.setStartEnterTime();
					AutoGroupUtility.sendWindowToPlayerIfOnline(lfp.getLeaderObjId(), maskId, 4);
					searchAndRemoveAdditionalRegistrations(lfp.getLeaderObjId());
					return;
				}
			}
		}
	}

	private void searchAndRemoveAdditionalRegistrations(int objectId) {
		List<LookingForParty> partiesToRemove = getSearchEntries(objectId);
		for (LookingForParty lfp : partiesToRemove) {
			int maskId = lfp.getMaskId();
			if (lfp.isLeader(objectId)) {
				removeSearchEntry(lfp);
				penaliseParty(lfp);
				lfp.getMembers().keySet().forEach(id -> AutoGroupUtility.sendWindowToPlayerIfOnline(id, maskId, 2));
			} else {
				lfp.unregisterMember(objectId);
				AutoGroupUtility.sendWindowToPlayerIfOnline(objectId, maskId, 2);
				penalisePlayerAndScheduleRemoval(objectId);
				checkQueueForNewMatches(maskId);
			}
		}
	}

	public void pressEnter(Player player, int instanceMaskId) {
		AutoInstance instance = getAutoInstance(player, instanceMaskId);
		if (instance == null)
			return;

		if (player.isInGroup())
			PlayerGroupService.removePlayer(player);
		if (player.isInAlliance())
			PlayerAllianceService.removePlayer(player);

		instance.onPressEnter(player);
		PacketSendUtility.sendPacket(player, new SM_AUTO_GROUP(instanceMaskId, 5));
	}

	public void onEnterInstance(Player player) {
		if (player.isInInstance()) {
			int obj = player.getObjectId();
			AutoInstance autoInstance = autoInstances.get(player.getWorldMapInstance());
			if (autoInstance != null && autoInstance.getRegisteredAGPlayers().containsKey(obj))
				autoInstance.onEnterInstance(player);
		}
	}

	public void cancelEnter(Player player, int instanceMaskId) {
		AutoInstance autoInstance = getAutoInstance(player, instanceMaskId);
		if (autoInstance != null) {
			int objectId = player.getObjectId();
			autoInstance.unregister(player);
			penalisePlayerAndScheduleRemoval(objectId);
			destroyOrAddPlayersFromQuickEntries(autoInstance);
			PacketSendUtility.sendPacket(player, new SM_AUTO_GROUP(instanceMaskId, 2));
		}
	}

	public void onPlayerLogin(Player player) {
		PeriodicInstanceManager.getInstance().checkAndSendOpenRegistrations(player);
	}

	public boolean isSearching(Player player, int maskId) {
		return getSearchEntry(player.getObjectId(), lookingParties.get(maskId)) != null;
	}

	private LookingForParty getSearchEntry(Player player, int maskId) {
		return getSearchEntry(player.getObjectId(), lookingParties.get(maskId));
	}

	private LookingForParty getSearchEntry(int playerObjectId, List<LookingForParty> parties) {
		if (parties != null) {
			synchronized (parties) {
				for (LookingForParty lfp : parties)
					if (lfp.isMember(playerObjectId))
						return lfp;
			}
		}
		return null;
	}

	private List<LookingForParty> getSearchEntries(int playerObjectId) {
		return lookingParties.values().stream().map(parties -> getSearchEntry(playerObjectId, parties)).filter(Objects::nonNull).toList();
	}

	public void onLogout(Player player) {
		int objectId = player.getObjectId();
		for (LookingForParty lfp : getSearchEntries(objectId)) {
			if (lfp.isOnStartEnterTask()) {
				for (AutoInstance autoInstance : autoInstances.values()) {
					cancelEnter(player, autoInstance.getAutoGroupType().getTemplate().getMaskId());
				}
			} else if (lfp.isLeader(objectId)) {
				lfp.setLeaderObjId(lfp.getMembers().keySet().stream().filter(id -> id != objectId).findFirst().orElse(0));
				if (lfp.getLeaderObjId() == 0) {
					removeSearchEntry(lfp);
				}
			} else {
				lfp.unregisterMember(objectId);
				checkQueueForNewMatches(lfp.getMaskId());
			}
		}

		AutoInstance autoInstance = autoInstances.get(player.getWorldMapInstance());
		if (autoInstance != null && autoInstance.getRegisteredAGPlayers().containsKey(objectId)) {
			destroyIfPossible(autoInstance);
		}
	}

	private void removeSearchEntry(LookingForParty lfp) {
		List<LookingForParty> lfps = lookingParties.get(lfp.getMaskId());
		synchronized (lfps) {
			lfps.remove(lfp);
		}
	}

	public void onLeaveInstance(Player player) {
		AutoInstance autoInstance = autoInstances.get(player.getWorldMapInstance());
		if (autoInstance != null && autoInstance.getRegisteredAGPlayers().containsKey(player.getObjectId())) {
			autoInstance.onLeaveInstance(player);
			destroyOrAddPlayersFromQuickEntries(autoInstance);
		}
		PeriodicInstanceManager.getInstance().checkAndSendOpenRegistrations(player);
	}

	private boolean canRegister(Player player, EntryRequestType ert, AutoGroupType agt) {
		int mapId = agt.getTemplate().getInstanceMapId();
		int instanceMaskId = agt.getTemplate().getMaskId();
		if (!agt.isInLvlRange(player.getLevel())) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_CANT_INSTANCE_ENTER_LEVEL());
			return false;
		} else if ((agt.isPvPFFAArena() || agt.isPvPSoloArena() || agt.isHarmonyArena() || agt.isGloryArena())
			&& !PvPArenaService.isPvPArenaAvailable(player, agt)) {
			return false;
		} else if (AutoGroupUtility.hasCoolDown(player, mapId)) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_CANNOT_MAKE_INSTANCE_COOL_TIME());
			return false;
		}
		return switch (ert) {
			case NEW_GROUP_ENTRY -> AutoGroupUtility.canRegisterNewEntry(player, agt);
			case QUICK_GROUP_ENTRY -> AutoGroupUtility.canRegisterQuickEntry(player, agt);
			case GROUP_ENTRY -> AutoGroupUtility.canRegisterGroupEntry(player, agt, mapId, instanceMaskId);
		};
	}

	private void penaliseParty(LookingForParty lfp) {
		lfp.getMembers().keySet().forEach(this::penalisePlayerAndScheduleRemoval);
	}

	private void penalisePlayerAndScheduleRemoval(int objectId) {
		if (penalties.add(objectId)) {
			ThreadPoolManager.getInstance().schedule(() -> {
				penalties.remove(objectId);
				PeriodicInstanceManager.getInstance().checkAndSendOpenRegistrations(objectId);
			}, 10000);
		}
	}

	public void stopRegistrationsByMaskId(int maskId) {
		List<LookingForParty> parties = lookingParties.remove(maskId);
		if (parties != null && !parties.isEmpty())
			parties.forEach(lfp -> lfp.getMembers().keySet().forEach(id -> AutoGroupUtility.sendWindowToPlayerIfOnline(id, maskId, 2)));
	}

	public void cancelRegistration(Player player, int maskId) {
		cancelRegistration(getSearchEntry(player, maskId), player, maskId);
	}

	public void cancelRegistration(LookingForParty lfp, Player player, int maskId) {
		int objectId = player.getObjectId();
		if (lfp != null) {
			if (lfp.isLeader(objectId)) {
				lookingParties.get(maskId).remove(lfp);
				penaliseParty(lfp);
				lfp.getMembers().keySet().forEach(id -> AutoGroupUtility.sendWindowToPlayerIfOnline(id, maskId, 2));
			} else {
				lfp.unregisterMember(objectId);
				AutoGroupUtility.sendWindowToPlayer(player, maskId, 2);
				penalisePlayerAndScheduleRemoval(objectId);
				checkQueueForNewMatches(maskId);
			}
		}
	}

	private void destroyOrAddPlayersFromQuickEntries(AutoInstance autoInstance) {
		if (!destroyIfPossible(autoInstance) && autoInstance.getAutoGroupType().getTemplate().canRegisterQuickEntry())
			checkQueueForQuickEntries(autoInstance);
	}

	public boolean destroyIfPossible(AutoInstance autoInstance) {
		WorldMapInstance instance = autoInstance.getInstance();
		if (autoInstance.getRegisteredAGPlayers().isEmpty() && instance.getPlayersInside().stream().noneMatch(Player::isOnline)) {
			autoInstances.remove(instance);
			InstanceService.destroyInstance(instance);
			return true;
		}
		return false;
	}

	private AutoInstance getAutoInstance(Player player, int instanceMaskId) {
		for (AutoInstance autoInstance : autoInstances.values())
			if (autoInstance.getAutoGroupType().getTemplate().getMaskId() == instanceMaskId
				&& autoInstance.getRegisteredAGPlayers().containsKey(player.getObjectId()))
				return autoInstance;
		return null;
	}

	public boolean isInAutoInstance(Player player) {
		return autoInstances.containsKey(player.getWorldMapInstance());
	}

	public static AutoGroupService getInstance() {
		return NewSingletonHolder.INSTANCE;
	}

	private static class NewSingletonHolder {

		private static final AutoGroupService INSTANCE = new AutoGroupService();
	}

}
