package com.aionemu.gameserver.services.rift;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Consumer;

import com.aionemu.gameserver.controllers.RVController;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.AionServerPacket;
import com.aionemu.gameserver.network.aion.serverpackets.SM_RIFT_ANNOUNCE;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.world.World;

/**
 * @author Source
 */
public class RiftInformer {

	public static void sendRiftsInfo(int worldId) {
		syncRiftsState(worldId, getPackets(worldId));
		int twinId = getTwinId(worldId);
		if (twinId > 0)
			syncRiftsState(twinId, getPackets(twinId));
	}

	public static void sendRiftsInfo(Player player) {
		syncRiftsState(player, getPackets(player.getWorldId()));
		int twinId = getTwinId(player.getWorldId());
		if (twinId > 0)
			syncRiftsState(twinId, getPackets(twinId));
	}

	public static void sendRiftInfo(int[] worlds) {
		for (int worldId : worlds)
			syncRiftsState(worldId, getPackets(worlds[0], -1));
	}

	public static void sendRiftDespawn(int worldId, int objId) {
		syncRiftsState(worldId, getPackets(worldId, objId), true);
	}

	private static List<AionServerPacket> getPackets(int worldId) {
		return getPackets(worldId, 0);
	}

	private static List<AionServerPacket> getPackets(int worldId, int objId) {
		List<AionServerPacket> packets = new ArrayList<>();
		if (objId == -1) {
			for (Npc rift : RiftManager.getSpawnedRifts(worldId)) {
				RVController controller = (RVController) rift.getController();
				if (!controller.isMaster()) {
					continue;
				}

				packets.add(new SM_RIFT_ANNOUNCE(controller, false));
			}
		} else if (objId > 0) {
			packets.add(new SM_RIFT_ANNOUNCE(objId));
		} else {
			packets.add(new SM_RIFT_ANNOUNCE(getAnnounceData(worldId)));
			for (Npc rift : RiftManager.getSpawnedRifts(worldId)) {
				RVController controller = (RVController) rift.getController();
				if (!controller.isMaster()) {
					continue;
				}
				packets.add(new SM_RIFT_ANNOUNCE(controller, true));
				packets.add(new SM_RIFT_ANNOUNCE(controller, false));
			}
		}
		return packets;
	}

	/*
	 * Sends generated rift info packets to player
	 */
	private static void syncRiftsState(Player player, final List<AionServerPacket> packets) {
		for (AionServerPacket packet : packets) {
			PacketSendUtility.sendPacket(player, packet);
		}
	}

	/*
	 * Sends generated rift info packets to all players within world
	 */
	private static void syncRiftsState(int worldId, final List<AionServerPacket> packets) {
		syncRiftsState(worldId, packets, false);
	}

	private static void syncRiftsState(int worldId, final List<AionServerPacket> packets, final boolean isDespawnInfo) {
		World.getInstance().getWorldMap(worldId).getMainWorldMapInstance().forEachPlayer(new Consumer<Player>() {

			@Override
			public void accept(Player player) {
				syncRiftsState(player, packets);
			}

		});
	}

	private static Map<Integer, Integer> getAnnounceData(int worldId) {
		Map<Integer, Integer> localRifts = new TreeMap<>();

		/**
		 * init empty list
		 * 12 different announces
		 * we have to send them all otherwise the client announces them even if they are not spawned
		 * looks like it depends on which map you are. But its always: first 6 indexes are from [current map] to [other map].
		 * whereas the last 6 indexes are from [other map] to [current map].
		 * index - announce
		 * 0 - normal rift
		 * 1 - vortex rift opened
		 * 2 - rift to concert hall
		 * 3 - rift to pangaea/ahserion
		 * 4 - volatile rift
		 * 5 - infiltration rift
		 *
		 * 6 - normal rift
		 * 7 - vortex rift opened
		 * 8 - rift to concert hall
		 * 9 - rift to pangaea/ahserion
		 * 10 - volatile rift
		 * 11 - infiltration rift
		 */
		for (int i = 0; i < 12; i++) {
			localRifts.put(i, 0);
		}


		for (Npc rift : RiftManager.getSpawnedRifts(worldId)) {
			RVController rc = (RVController) rift.getController();
			localRifts = calcRiftsData(rc, localRifts);
		}

		return localRifts;
	}

	private static Map<Integer, Integer> calcRiftsData(RVController rift, Map<Integer, Integer> local) {
		if (rift.isMaster()) {
			switch (rift.getRiftTemplate()) {
				case MARCHUTAN_AM:
				case KAISINEL_AM:
					local.put(1, local.get(1) + 1); // vortex rift
					break;
				case MORHEIM_AM:
				case MORHEIM_BM:
				case MORHEIM_CM:
				case MORHEIM_DM:
				case MORHEIM_EM:
				case MORHEIM_FM:
				case MORHEIM_GM:
				case BELUSLAN_AM:
				case BELUSLAN_BM:
				case BELUSLAN_CM:
				case BELUSLAN_DM:
				case BELUSLAN_EM:
				case BELUSLAN_FM:
				case BELUSLAN_GM:
				case GELKMAROS_AM:
				case GELKMAROS_BM:
				case GELKMAROS_CM:
				case GELKMAROS_DM:
				case ENSHAR_AM:
				case ENSHAR_BM:
				case ENSHAR_CM:
				case ENSHAR_DM:
				case ENSHAR_EM:
				case ENSHAR_FM:
				case ELTNEN_AM:
				case ELTNEN_BM:
				case ELTNEN_CM:
				case ELTNEN_DM:
				case ELTNEN_EM:
				case ELTNEN_FM:
				case ELTNEN_GM:
				case HEIRON_AM:
				case HEIRON_BM:
				case HEIRON_CM:
				case HEIRON_DM:
				case HEIRON_EM:
				case HEIRON_FM:
				case HEIRON_GM:
				case INGGISON_AM:
				case INGGISON_BM:
				case INGGISON_CM:
				case INGGISON_DM:
				case CYGNEA_AM:
				case CYGNEA_BM:
				case CYGNEA_CM:
				case CYGNEA_DM:
				case CYGNEA_EM:
				case CYGNEA_FM:
					local.put(0, local.get(0) + 1); // normal rift
					break;
				case ENSHAR_GM:
				case ENSHAR_HM:
				case ENSHAR_IM:
				case CYGNEA_GM:
				case CYGNEA_HM:
				case CYGNEA_IM:
					if (rift.isVolatile()) {
						local.put(4, local.get(4) + 1); // chaos rift
					} else {
						local.put(0, local.get(0) + 1); // normal rift
					}
					break;

			}
		}
		return local;
	}

	private static int getTwinId(int worldId) {
		switch (worldId) {
			case 110070000: // Kaisinel Academy -> Brusthonin
				return 220050000;
			case 210020000: // Eltnen -> Morheim
				return 220020000;
			case 210040000: // Heiron -> Beluslan
				return 220040000;
			case 210050000: // Inggison -> Gelkmaros
				return 220070000;
			case 210060000: // Theobomos -> Marchutan Priory
				return 120080000;
			case 210070000: // Cygnea -> Enshar
				return 220080000;
			case 120080000: // Marchutan Priory -> Theobomos
				return 210060000;
			case 220020000: // Morheim -> Eltnen
				return 210020000;
			case 220040000: // Beluslan -> Heiron
				return 210040000;
			case 220050000: // Brusthonin -> Kaisinel Academy
				return 110070000;
			case 220070000: // Gelkmaros -> Inggison
				return 210050000;
			case 220080000: // Enshar -> Cygnea
				return 210070000;
			default:
				return 0;
		}
	}

}
