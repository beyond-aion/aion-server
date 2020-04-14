package com.aionemu.gameserver.spawnengine;

import com.aionemu.gameserver.controllers.StaticObjectController;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.gameobjects.StaticObject;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.templates.VisibleObjectTemplate;
import com.aionemu.gameserver.model.templates.spawns.SpawnGroup;
import com.aionemu.gameserver.model.templates.spawns.SpawnTemplate;
import com.aionemu.gameserver.world.World;
import com.aionemu.gameserver.world.knownlist.PlayerAwareKnownList;

/**
 * @author ATracer
 */
public class StaticObjectSpawnManager {

	/**
	 * @param spawnGroup
	 * @param instanceIndex
	 */
	public static void spawnTemplate(SpawnGroup spawn, int instanceIndex) {
		VisibleObjectTemplate objectTemplate = DataManager.ITEM_DATA.getItemTemplate(spawn.getNpcId());
		if (objectTemplate == null)
			return;

		if (spawn.hasPool()) {
			spawn.resetTemplates(instanceIndex);
			for (int i = 0; i < spawn.getPool(); i++) {
				SpawnTemplate template = spawn.getRndTemplate(instanceIndex);
				StaticObject staticObject = new StaticObject(new StaticObjectController(), template, objectTemplate);
				staticObject.setKnownlist(new PlayerAwareKnownList(staticObject));
				bringIntoWorld(staticObject, template, instanceIndex);
			}
		} else {
			for (SpawnTemplate template : spawn.getSpawnTemplates()) {
				StaticObject staticObject = new StaticObject(new StaticObjectController(), template, objectTemplate);
				staticObject.setKnownlist(new PlayerAwareKnownList(staticObject));
				bringIntoWorld(staticObject, template, instanceIndex);
			}
		}
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
