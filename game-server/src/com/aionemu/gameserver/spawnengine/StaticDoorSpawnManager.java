package com.aionemu.gameserver.spawnengine;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.controllers.StaticObjectController;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.gameobjects.StaticDoor;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.templates.spawns.SpawnTemplate;
import com.aionemu.gameserver.model.templates.staticdoor.DoorType;
import com.aionemu.gameserver.model.templates.staticdoor.StaticDoorTemplate;
import com.aionemu.gameserver.utils.idfactory.IDFactory;
import com.aionemu.gameserver.world.World;
import com.aionemu.gameserver.world.knownlist.PlayerAwareKnownList;

/**
 * @author MrPoke
 */
public class StaticDoorSpawnManager {

	private static Logger log = LoggerFactory.getLogger(StaticDoorSpawnManager.class);

	/**
	 * @param spawnGroup
	 * @param instanceIndex
	 */
	public static void spawnTemplate(int worldId, int instanceIndex) {
		int counter = 0;
		for (StaticDoorTemplate data : DataManager.STATICDOOR_DATA.getStaticDoors(worldId)) {
			if (data.getDoorType() != DoorType.DOOR) {
				// TODO: assign house doors to houses, so geo doors could be triggered by changing house settings;
				continue;
			}
			SpawnTemplate spawn = SpawnEngine.newSingleTimeSpawn(worldId, 300001, data.getX(), data.getY(), data.getZ(), (byte) 0);
			spawn.setStaticId(data.getDoorId());
			int objectId = IDFactory.getInstance().nextId();
			StaticDoor staticDoor = new StaticDoor(objectId, new StaticObjectController(), spawn, data, instanceIndex);
			staticDoor.setKnownlist(new PlayerAwareKnownList(staticDoor));
			bringIntoWorld(staticDoor, spawn, instanceIndex);
			counter++;
		}
		if (counter > 0)
			log.info("Spawned static doors " + worldId + " [" + instanceIndex + "]: " + counter);
	}

	/**
	 * @param visibleObject
	 * @param spawn
	 * @param instanceIndex
	 */
	private static void bringIntoWorld(VisibleObject visibleObject, SpawnTemplate spawn, int instanceIndex) {
		World world = World.getInstance();
		world.storeObject(visibleObject);
		world.setPosition(visibleObject, spawn.getWorldId(), instanceIndex, spawn.getX(), spawn.getY(), spawn.getZ(), spawn.getHeading());
		world.spawn(visibleObject);
	}
}
