package com.aionemu.gameserver.questEngine.task.checker;

import com.aionemu.gameserver.model.gameobjects.Creature;

/**
 * @author ATracer, Neon
 */
public class ZoneChecker extends DestinationChecker {

	public static final String FOLLOWING_AI_NAME = "following";

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
