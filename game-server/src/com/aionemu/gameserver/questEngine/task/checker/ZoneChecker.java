package com.aionemu.gameserver.questEngine.task.checker;

import com.aionemu.gameserver.model.gameobjects.Creature;

/**
 * @author ATracer, Neon
 */
public class ZoneChecker extends DestinationChecker {

	protected final String zoneName;

	public ZoneChecker(Creature follower, String zoneName) {
		super(follower);
		this.zoneName = zoneName;
	}

	@Override
	public boolean check() {
		return follower.isInsideZone(zoneName);
	}
}
