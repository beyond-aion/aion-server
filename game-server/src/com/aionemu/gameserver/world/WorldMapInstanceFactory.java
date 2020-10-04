package com.aionemu.gameserver.world;

import com.aionemu.gameserver.instance.InstanceEngine;
import com.aionemu.gameserver.instance.handlers.GeneralInstanceHandler;
import com.aionemu.gameserver.instance.handlers.InstanceHandler;

/**
 * @author ATracer
 */
public class WorldMapInstanceFactory {

	public static WorldMapInstance createWorldMapInstance(WorldMap parent, int instanceId, int maxPlayers) {
		return createWorldMapInstance(parent, instanceId, 0, null, maxPlayers);
	}

	public static WorldMapInstance createWorldMapInstance(WorldMap parent, int instanceId, int ownerId, GeneralInstanceHandler customHandler, int maxPlayers) {
		WorldMapInstance worldMapInstance;
		if (parent.getMapId() == WorldMapType.RESHANTA.getId()) {
			worldMapInstance = new WorldMap3DInstance(parent, instanceId, maxPlayers);
		} else {
			worldMapInstance = new WorldMap2DInstance(parent, instanceId, ownerId, maxPlayers);
		}

		if (customHandler != null) {
			worldMapInstance.setInstanceHandler(customHandler);
		} else {
			InstanceHandler instanceHandler = InstanceEngine.getInstance().getNewInstanceHandler(parent.getMapId());
			worldMapInstance.setInstanceHandler(instanceHandler);
		}
		return worldMapInstance;
	}
}
