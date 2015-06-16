package com.aionemu.gameserver.spawnengine;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Rolandas
 */
public class WorldWalkerFormations {

	private Map<Integer, InstanceWalkerFormations> formations;

	public WorldWalkerFormations() {
		formations = new ConcurrentHashMap<>();
	}

	/**
	 * @param instanceId
	 * @return
	 */
	protected InstanceWalkerFormations getInstanceFormations(int instanceId) {
		InstanceWalkerFormations instanceFormation = formations.get(instanceId);
		if (instanceFormation == null) {
			instanceFormation = new InstanceWalkerFormations();
			formations.put(instanceId, instanceFormation);
		}
		return instanceFormation;
	}

}
