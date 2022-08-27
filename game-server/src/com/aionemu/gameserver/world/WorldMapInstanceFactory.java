package com.aionemu.gameserver.world;

import java.util.function.Function;

import com.aionemu.gameserver.instance.InstanceEngine;
import com.aionemu.gameserver.instance.handlers.InstanceHandler;

/**
 * @author ATracer
 */
public class WorldMapInstanceFactory {

	public static WorldMapInstance createWorldMapInstance(WorldMap parent, int maxPlayers) {
		return createWorldMapInstance(parent, 0, InstanceEngine.getInstance()::getNewInstanceHandler, maxPlayers);
	}

	public static WorldMapInstance createWorldMapInstance(WorldMap parent, int ownerId, Function<WorldMapInstance, InstanceHandler> instanceHandlerSupplier, int maxPlayers) {
		WorldMapInstance instance;
		if (parent.getMapId() == WorldMapType.RESHANTA.getId()) {
			instance = new WorldMap3DInstance(parent, parent.getNextInstanceId(), maxPlayers, instanceHandlerSupplier);
		} else {
			instance = new WorldMap2DInstance(parent, parent.getNextInstanceId(), ownerId, maxPlayers, instanceHandlerSupplier);
		}
		parent.addInstance(instance.getInstanceId(), instance);
		return instance;
	}
}
