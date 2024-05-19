package com.aionemu.gameserver.spawnengine;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.controllers.StaticObjectController;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.gameobjects.StaticDoor;
import com.aionemu.gameserver.model.templates.spawns.SpawnTemplate;
import com.aionemu.gameserver.model.templates.staticdoor.StaticDoorTemplate;
import com.aionemu.gameserver.world.WorldMapInstance;
import com.aionemu.gameserver.world.geo.GeoService;
import com.aionemu.gameserver.world.knownlist.PlayerAwareKnownList;

/**
 * @author MrPoke
 */
public class StaticDoorSpawnManager {

	private static final Logger log = LoggerFactory.getLogger(StaticDoorSpawnManager.class);

	public static void spawnTemplate(WorldMapInstance instance) {
		int counter = 0;
		for (StaticDoorTemplate data : DataManager.STATICDOOR_DATA.getStaticDoors(instance.getMapId())) {
			SpawnTemplate spawn = SpawnEngine.newSingleTimeSpawn(instance.getMapId(), 300001, data.getX(), data.getY(), data.getZ(), (byte) 0);
			spawn.setStaticId(data.getId());
			StaticDoor staticDoor = new StaticDoor(new StaticObjectController(), spawn, data, instance.getInstanceId());
			staticDoor.setKnownlist(new PlayerAwareKnownList(staticDoor));
			SpawnEngine.bringIntoWorld(staticDoor, spawn, instance.getInstanceId());
			counter++;
			GeoService.getInstance().setDoorState(instance.getMapId(), instance.getInstanceId(), data.getId(), staticDoor.isOpen());
		}
		if (counter > 0)
			log.info("Spawned " + counter + " static doors in " + instance);
	}
}
