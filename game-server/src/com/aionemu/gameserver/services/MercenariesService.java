package com.aionemu.gameserver.services;

import java.util.List;

import javolution.util.FastTable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.siege.SiegeNpc;
import com.aionemu.gameserver.model.siege.FortressLocation;
import com.aionemu.gameserver.model.templates.spawns.Spawn;
import com.aionemu.gameserver.model.templates.spawns.SpawnSpotTemplate;
import com.aionemu.gameserver.model.templates.spawns.SpawnTemplate;
import com.aionemu.gameserver.model.templates.spawns.mercenaries.MercenaryRace;
import com.aionemu.gameserver.model.templates.spawns.mercenaries.MercenarySpawn;
import com.aionemu.gameserver.model.templates.spawns.mercenaries.MercenaryZone;
import com.aionemu.gameserver.network.aion.serverpackets.SM_DIALOG_WINDOW;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.spawnengine.SpawnEngine;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author ViAl
 * @modified Whoop
 */
public class MercenariesService {

	private static final Logger log = LoggerFactory.getLogger(MercenariesService.class);

	public static void checkMercenaryZone(Player player, SiegeNpc owner, long itemCount, int siegeId, int msgId, int shoutId, int zoneId) {
		if (!hasRequiredItems(player, owner, itemCount))
			return;
		MercenaryZone zone = getZone(siegeId, zoneId, owner.getRace());
		if (zone != null) { // could be null if not implemented in spawns
			player.getInventory().decreaseByItemId(186000236, itemCount);
			PacketSendUtility.sendPacket(player, new SM_SYSTEM_MESSAGE(msgId));
			PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(owner.getObjectId(), 2375));
			NpcShoutsService.getInstance().sendMsg(owner, shoutId);
			FortressLocation loc = SiegeService.getInstance().getFortress(zone.getSiegeId());
			loc.despawnMercenaries(zone.getZoneId());
			// DAOManager.getDAO(SiegeMercenariesDAO.class).deleteMercenaries(siegeId, zoneId);
			List<VisibleObject> mercs = spawn(zone);
			loc.addMercenaries(zone.getZoneId(), mercs);
			// DAOManager.getDAO(SiegeMercenariesDAO.class).insertMercenaries(siegeId, zoneId, owner.getRace());
		}
	}

	private static boolean hasRequiredItems(Player player, SiegeNpc owner, long itemCount) {
		long count = player.getInventory().getItemCountByItemId(186000236);
		if (count < itemCount) {
			PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(owner.getObjectId(), 27));
			return false;
		}
		return true;
	}

	private static MercenaryZone getZone(int siegeId, int zoneId, Race race) {
		MercenaryZone zone = null;
		MercenarySpawn spawn = DataManager.SPAWNS_DATA2.getMercenarySpawnBySiegeId(siegeId);
		if (spawn == null) {
			log.info("There is no mercenaries spawns for siege " + siegeId + " and zone" + zoneId);
			return zone;
		}
		MercenaryRace targetRace = null;
		for (MercenaryRace mrace : spawn.getMercenaryRaces()) {
			if (mrace.getRace() == race) {
				targetRace = mrace;
				break;
			}
		}
		if (targetRace == null) {
			log.info("There is no mercenary race for siege " + siegeId + ", zone" + zoneId + ", race:" + race.toString());
			return zone;
		}
		for (MercenaryZone mzone : targetRace.getMercenaryZones()) {
			if (mzone.getZoneId() == zoneId) {
				zone = mzone;
				break;
			}
		}
		if (zone == null) {
			log.info("There is no mercenary zone for siege " + siegeId + ", zone" + zoneId + ", race:" + race.toString());
			return zone;
		}
		return zone;
	}

	private static List<VisibleObject> spawn(MercenaryZone zone) {
		List<VisibleObject> mercs = new FastTable<VisibleObject>();
		for (Spawn spawn : zone.getSpawns()) {
			for (SpawnSpotTemplate sst : spawn.getSpawnSpotTemplates()) {
				SpawnTemplate spawnTemplate = SpawnEngine.addNewSingleTimeSpawn(zone.getWorldId(), spawn.getNpcId(), sst.getX(), sst.getY(), sst.getZ(),
					sst.getHeading());
				VisibleObject newMerc = SpawnEngine.spawnObject(spawnTemplate, 1);
				mercs.add(newMerc);
				log.info("Spawning mercenary, npc id " + spawn.getNpcId() + ", at " + zone.getWorldId() + " " + sst.getX() + " " + sst.getY() + " "
					+ sst.getZ());
			}
		}
		return mercs;
	}

	/**
	 * Should be called only from DAO
	 * 
	 * @param locationId
	 * @param zoneId
	 * @param race
	 */
	public static void loadMercenaries(int locationId, int zoneId, Race race) {
		MercenaryZone zone = getZone(locationId, zoneId, race);
		if (zone != null) { // could be null if not implemented in spawns
			FortressLocation loc = SiegeService.getInstance().getFortress(zone.getSiegeId());
			List<VisibleObject> mercs = spawn(zone);
			loc.addMercenaries(zone.getZoneId(), mercs);
		}
	}
}
