package com.aionemu.gameserver.services;

import java.util.List;
import java.util.Map;

import javolution.util.FastMap;
import javolution.util.FastTable;

import com.aionemu.gameserver.configs.main.CustomConfig;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.serial_killer.RankRestriction;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SERIAL_KILLER;
import com.aionemu.gameserver.services.serialkillers.SerialKiller;
import com.aionemu.gameserver.services.serialkillers.SerialKillerDebuff;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.utils.MathUtil;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.world.World;
import com.aionemu.gameserver.world.knownlist.Visitor;

/**
 * @author Source
 * @modified Dtem, ginho
 */
public class SerialKillerService {

	private final Map<Integer, SerialKiller> serialKillers = new FastMap<>();
	private static final Map<Integer, WorldType> handledWorlds = new FastMap<>();
	private final int refresh = CustomConfig.SERIALKILLER_REFRESH;
	private final int levelDiff = CustomConfig.SERIALKILLER_LEVEL_DIFF;
	private SerialKillerDebuff buff;

	public enum WorldType {
		ASMODIANS,
		ELYOS,
		USEALL;
	}

	public void initSerialKillers() {
		if (!CustomConfig.SERIALKILLER_ENABLED)
			return;
		buff = new SerialKillerDebuff();

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
						continue;
					}
					if (info.victims > 0) {
						info.victims -= CustomConfig.SERIALKILLER_DECREASE;

						if (info.victims < 1) {
							removeSerialKiller(info.getOwner());
							continue;
						}
						int newRank = getKillerRank(info.victims);

						if (info.getRank() != newRank) {
							info.setRank(newRank);
							boolean isEnemyWorld = isEnemyWorld(info.getOwner());

							PacketSendUtility.sendPacket(info.getOwner(), new SM_SERIAL_KILLER(isEnemyWorld ? 1 : 7, info.getRank()));

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
		}, refresh * 60000, refresh * 60000); // kills remove timer
	}

	public List<Player> getWorldKillers(Player player) {
		List<Player> killers = new FastTable<>();
		for (SerialKiller sk : serialKillers.values())
			if (MathUtil.isIn3dRange(sk.getOwner(), player, 500))
				if (sk.getOwner().getRace() != player.getRace())
					killers.add(sk.getOwner());
		return killers;
	}

	public void onLogin(Player player) {
		if (!CustomConfig.SERIALKILLER_ENABLED)
			return;
		if (serialKillers.containsKey(player.getObjectId())) {
			boolean isEnemyWorld = isEnemyWorld(player);
			player.setSKInfo(serialKillers.get(player.getObjectId()));
			player.getSKInfo().refreshOwner(player);
			PacketSendUtility.sendPacket(player, new SM_SERIAL_KILLER(isEnemyWorld ? 1 : 7, player.getSKInfo().getRank()));
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

	public void onKillSerialKiller(final Player killer, final Player victim) {

	}

	public void intruderScan(Player player) {
		int worldId = player.getWorldId();
		if (!isHandledWorld(worldId))
			return;
		PacketSendUtility.sendPacket(player, new SM_SERIAL_KILLER(getWorldKillers(player)));
	}

	public void updateRank(final Player killer, Player victim) {
		if (!CustomConfig.SERIALKILLER_ENABLED)
			return;
		if (isHandledWorld(killer.getWorldId())) {
			SerialKiller info = killer.getSKInfo();

			if (killer.getLevel() >= victim.getLevel() - levelDiff) {
				int rank = getKillerRank(++info.victims);

				if (info.getRank() != rank) {
					boolean isEnemyWorld = isEnemyWorld(killer);

					info.setRank(rank);
					buff.applyEffect(killer, isEnemyWorld ? "KILLER" : "GUARD", killer.getRace(), rank);

					PacketSendUtility.sendPacket(killer, new SM_SERIAL_KILLER(isEnemyWorld ? 1 : 7, info.getRank()));

					killer.getKnownList().doOnAllPlayers(new Visitor<Player>() {

						@Override
						public void visit(Player observed) {
							PacketSendUtility.sendPacket(observed, new SM_SERIAL_KILLER(isEnemyWorld ? 6 : 9, killer));
						}
					});
				}
				if (!serialKillers.containsKey(killer.getObjectId()))
					serialKillers.put(killer.getObjectId(), info);
			}
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
		buff.endEffect(player);
		if (serialKillers.containsKey(player.getObjectId()))
			serialKillers.remove(player.getObjectId());
	}

	public static SerialKillerService getInstance() {
		return SerialKillerService.SingletonHolder.instance;
	}

	private static class SingletonHolder {

		protected static final SerialKillerService instance = new SerialKillerService();
	}

}
