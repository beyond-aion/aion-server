package com.aionemu.gameserver.services;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.aionemu.gameserver.configs.main.CustomConfig;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.legionDominion.LegionDominionLocation;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SERIAL_KILLER;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.serialkillers.SerialKiller;
import com.aionemu.gameserver.utils.MathUtil;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.world.World;
import com.aionemu.gameserver.world.knownlist.Visitor;
import com.aionemu.gameserver.world.zone.ZoneInstance;

import javolution.util.FastMap;
import javolution.util.FastTable;

/**
 * @author Source
 * @modified Dtem, ginho
 * @reworked Yeats 17.01.2016
 */
public class SerialKillerService {

	private final Map<Integer, SerialKiller> serialKillers = new FastMap<>();
	private final Map<Integer, Long> cooldowns = new ConcurrentHashMap<Integer, Long>(); //cd for intruder scan
	private final List<Integer> ldPlayers = new FastTable<>();
	private static final Map<Integer, WorldType> handledWorlds = new FastMap<>();
	private final int refresh = CustomConfig.SERIALKILLER_REFRESH;
	private final int levelDiff = CustomConfig.SERIALKILLER_LEVEL_DIFF;

	public enum WorldType {
		ASMODIANS,
		ELYOS,
		USEALL;
	}

	public void initSerialKillers() {
		if (!CustomConfig.SERIALKILLER_ENABLED)
			return;

		for (String world : CustomConfig.SERIALKILLER_WORLDS.split(",")) {
			if ("".equals(world))
				break;
			int worldId = Integer.parseInt(world);
			int worldType = Integer.parseInt(String.valueOf(world.charAt(1)));
			WorldType type = worldType > 0 ? worldType > 1 ? WorldType.ASMODIANS : WorldType.ELYOS : WorldType.USEALL;
			handledWorlds.put(worldId, type);
		}

		ThreadPoolManager.getInstance().scheduleAtFixedRate(new Runnable() {

			@Override
			public void run() {
				for (SerialKiller info : serialKillers.values()) {
					if (!info.getOwner().isOnline() || !isHandledWorld(info.getOwner().getWorldId())) {
						removeSerialKiller(info.getOwner());
						getOrRemoveCooldown(info.getOwner().getObjectId());
						continue;
					}
					getOrRemoveCooldown(info.getOwner().getObjectId());
					if (info.victims > 0) {
						info.victims -= CustomConfig.SERIALKILLER_DECREASE;

						if (info.victims < 1) {
							removeSerialKiller(info.getOwner());
							continue;
						}
						int newRank = getKillerRank(info.victims);

						if (info.getRank() != newRank) {
							info.setRank(newRank);
							if (!ldPlayers.contains(info.getOwner().getObjectId())) {
								boolean isEnemyWorld = isEnemyWorld(info.getOwner());

								PacketSendUtility.sendPacket(info.getOwner(), new SM_SERIAL_KILLER(isEnemyWorld ? 1 : 8, info.getRank()));

								info.getOwner().getKnownList().doOnAllPlayers(new Visitor<Player>() {

									@Override
									public void visit(Player observed) {
										PacketSendUtility.sendPacket(observed, new SM_SERIAL_KILLER(isEnemyWorld ? 6 : 7, info.getOwner()));
									}
								});
							}
						}
					}
				}
			}
		}, refresh * 60000, refresh * 60000); // kills remove timer
	}

	public List<Player> getWorldKillers(Player player) {
		List<Player> killers = new FastTable<>();
		for (SerialKiller sk : serialKillers.values()) {
			int rank = player.getSKInfo().getLDRank() > player.getSKInfo().getRank() ?  player.getSKInfo().getLDRank() : player.getSKInfo().getRank();
			//ok this should never happen but anyways.
			if (rank == 0) {
				break;
			} else if (rank == 1 && sk.getRank() != 3) { //protectors rank 1 can only see intruders rank 3
				continue;
			} else if (rank == 2 && sk.getRank() == 1) {//protectors rank 2 can only see intruders rank 2 & 3
				continue;
			} else { //protectors rank 3 & stonespear owner can see all ranks
				if (MathUtil.isIn3dRange(sk.getOwner(), player, 500))
					if (sk.getOwner().getRace() != player.getRace())
						killers.add(sk.getOwner());
				}
		}
		return killers;
	}

	public void onLogin(Player player) {
		if (!CustomConfig.SERIALKILLER_ENABLED)
			return;
		if (serialKillers.containsKey(player.getObjectId())) {
			boolean isEnemyWorld = isEnemyWorld(player);
			player.setSKInfo(serialKillers.get(player.getObjectId()));
			player.getSKInfo().refreshOwner(player);
			if (ldPlayers.contains(player.getObjectId())) {
				PacketSendUtility.sendPacket(player, new SM_SERIAL_KILLER(7, player.getSKInfo().getLDRank(), getOrRemoveCooldown(player.getObjectId())));
			} else { 
				PacketSendUtility.sendPacket(player, new SM_SERIAL_KILLER(isEnemyWorld ? 0 : 7, player.getSKInfo().getRank(), getOrRemoveCooldown(player.getObjectId())));
			}
		}
	}

	public void onLogout(Player player) {
		if (!CustomConfig.SERIALKILLER_ENABLED) {
			return;
		}

		onLeaveMap(player);
	}

	public void onEnterMap(final Player player) {

	}

	public void onLeaveMap(Player player) {
		if (!CustomConfig.SERIALKILLER_ENABLED)
			return;

		removeSerialKiller(player);
	}

	//for Legion Dominion Zones
	public void onEnterZone(Player player, ZoneInstance zone) {
		if (!CustomConfig.SERIALKILLER_ENABLED)
			return;
		if (zone.isDominionZone()) {
			if (player.getLegion() != null && player.getLegion().getOccupiedLegionDominion() > 0) {
				LegionDominionLocation loc = LegionDominionService.getInstance().getLegionDominionByZone(zone.getAreaTemplate().getZoneName().name());
				if (loc != null && loc.getLegionId() == player.getLegion().getLegionId()) {
					if (!ldPlayers.contains(player.getObjectId())) {
						ldPlayers.add(player.getObjectId());
						player.getSKInfo().setLDRank(3);
						player.getSKInfo().getBuff().applyEffect(player, "GUARD", player.getRace(), 3);
						PacketSendUtility.sendPacket(player, new SM_SERIAL_KILLER(7, player.getSKInfo().getLDRank(), getOrRemoveCooldown(player.getObjectId())));
						if (!serialKillers.containsKey(player.getObjectId())) {
							serialKillers.put(player.getObjectId(), player.getSKInfo());
						}
					}
				}
			}
		}

	}
	
	//for Legion Dominion Zones
	public void onLeaveZone(Player player, ZoneInstance zone) {
		if (!CustomConfig.SERIALKILLER_ENABLED)
			return;
		if (zone.isDominionZone() && ldPlayers.contains(player.getObjectId())) {
			ldPlayers.remove(player.getObjectId());
			player.getSKInfo().getBuff().endEffect(player);
			player.getSKInfo().setLDRank(0);
			updateRank(player);
		}
	}
	
	//update victims rank
	public void onKillSerialKiller(final Player killer, final Player victim) {
		if (!CustomConfig.SERIALKILLER_ENABLED)
			return;
		if (isHandledWorld(victim.getWorldId())) {
			SerialKiller info = victim.getSKInfo();
			if (killer.getLevel() - victim.getLevel() <= levelDiff) {
				if (info.victims > 0) {
				int rank = getKillerRank(--info.victims);
				boolean isEnemyWorld = isEnemyWorld(victim);
				if (isEnemyWorld && info.getRank() == 3) { //old rank
					World.getInstance().getWorldMap(victim.getWorldId()).getMainWorldMapInstance().doOnAllPlayers(new Visitor<Player> () {
						
						@Override
						public void visit(Player player) {
							PacketSendUtility.sendPacket(player, new SM_SYSTEM_MESSAGE(killer.getRace() == Race.ASMODIANS ? 1400141 : 1400142, killer.getName(), victim.getName()));
						}
					});
				}
				if (info.getRank() != rank) {
					info.setRank(rank);
					if (!ldPlayers.contains(victim.getObjectId())) {
						victim.getSKInfo().getBuff().endEffect(victim);
						victim.getSKInfo().getBuff().applyEffect(victim, isEnemyWorld ? "KILLER" : "GUARD", victim.getRace(), rank);

						PacketSendUtility.sendPacket(victim, new SM_SERIAL_KILLER(isEnemyWorld ? 1 : 8, info.getRank()));

						victim.getKnownList().doOnAllPlayers(new Visitor<Player>() {

							@Override
							public void visit(Player observed) {
								PacketSendUtility.sendPacket(observed, new SM_SERIAL_KILLER(isEnemyWorld ? 6 : 9, victim));
							}
						});
					}
				}
				if (!serialKillers.containsKey(victim.getObjectId()))
					serialKillers.put(victim.getObjectId(), info);
				}
			}
		}
	}

	public void intruderScan(Player player) {
		int worldId = player.getWorldId();
		if (!isHandledWorld(worldId))
			return;
		if (getOrRemoveCooldown(player.getObjectId()) > 0) {
			return;
		}
		cooldowns.put(player.getObjectId(), System.currentTimeMillis() + 180 * 1000);
		PacketSendUtility.sendPacket(player, new SM_SERIAL_KILLER(getWorldKillers(player)));
	}

	private int getOrRemoveCooldown(int objectId) {
		if (cooldowns.containsKey(objectId)) {
			int estimated = (int) ((cooldowns.get(objectId) - System.currentTimeMillis()) / 1000);
			if (estimated > 0) {
				return estimated;
			}
			else {
				cooldowns.remove(objectId);
				return 0;
			}
		}
		return 0;
	}

	//update killers rank
	public void updateRank(final Player killer, Player victim) {
		if (!CustomConfig.SERIALKILLER_ENABLED)
			return;
		if (isHandledWorld(killer.getWorldId())) {
			getOrRemoveCooldown(killer.getObjectId());
			SerialKiller info = killer.getSKInfo();
			if (killer.getLevel() - victim.getLevel() <= levelDiff) {
				int rank = getKillerRank(++info.victims);

				if (info.getRank() != rank) {
				boolean isEnemyWorld = isEnemyWorld(killer);

					info.setRank(rank);
					if (!ldPlayers.contains(killer.getObjectId())) {
						killer.getSKInfo().getBuff().applyEffect(killer, isEnemyWorld ? "KILLER" : "GUARD", killer.getRace(), rank);

						PacketSendUtility.sendPacket(killer, new SM_SERIAL_KILLER(isEnemyWorld ? 1 : 8, info.getRank()));

						killer.getKnownList().doOnAllPlayers(new Visitor<Player>() {

							@Override
							public void visit(Player observed) {
								PacketSendUtility.sendPacket(observed, new SM_SERIAL_KILLER(isEnemyWorld ? 6 : 9, killer));
							}
						});
					}
				}
				if (!serialKillers.containsKey(killer.getObjectId()))
					serialKillers.put(killer.getObjectId(), info);
			}
		}
	}
	
	private void updateRank(Player player) {
		if (!CustomConfig.SERIALKILLER_ENABLED)
			return;
		if (isHandledWorld(player.getWorldId())) {
			SerialKiller info = player.getSKInfo();
			boolean isEnemyWorld = isEnemyWorld(player);
			getOrRemoveCooldown(player.getObjectId());
			if (info.getRank() <= 0) {
				player.getSKInfo().getBuff().endEffect(player);
			} else {
				player.getSKInfo().getBuff().applyEffect(player, isEnemyWorld ? "KILLER" : "GUARD", player.getRace(), info.getRank());
			}
			
			PacketSendUtility.sendPacket(player, new SM_SERIAL_KILLER(isEnemyWorld ? 1 : 8, info.getRank()));

			player.getKnownList().doOnAllPlayers(new Visitor<Player>() {

				@Override
				public void visit(Player observed) {
					PacketSendUtility.sendPacket(observed, new SM_SERIAL_KILLER(isEnemyWorld ? 6 : 9, player));
				}
			});
		}
	}

	private int getKillerRank(int kills) {
		if (kills >= CustomConfig.KILLER_3ND_RANK_KILLS)
			return 3;
		if (kills >= CustomConfig.KILLER_2ND_RANK_KILLS)
			return 2;
		if (kills >= CustomConfig.KILLER_1ST_RANK_KILLS)
			return 1;
		return 0;
	}

	public boolean isRestrictPortal(Player killer) {
		return false;
	}

	public boolean isRestrictDynamicBindstone(Player killer) {
		return false;
	}

	public boolean isHandledWorld(int worldId) {
		return handledWorlds.containsKey(worldId);
	}

	public boolean isEnemyWorld(Player player) {
		if (isHandledWorld(player.getWorldId())) {
			WorldType homeType = player.getRace().equals(Race.ASMODIANS) ? WorldType.ASMODIANS : WorldType.ELYOS;
			return !handledWorlds.get(player.getWorldId()).equals(homeType);
		}
		return false;
	}

	public void removeSerialKiller(Player player) {
		player.getSKInfo().victims = 0;
		player.getSKInfo().setRank(0);
		player.getSKInfo().setLDRank(0);
		player.getSKInfo().getBuff().endEffect(player);
		if (serialKillers.containsKey(player.getObjectId()))
			serialKillers.remove(player.getObjectId());
		if (ldPlayers.contains(player.getObjectId()))
			ldPlayers.remove(player.getObjectId());
	}

	public static SerialKillerService getInstance() {
		return SerialKillerService.SingletonHolder.instance;
	}

	private static class SingletonHolder {

		protected static final SerialKillerService instance = new SerialKillerService();
	}

}
