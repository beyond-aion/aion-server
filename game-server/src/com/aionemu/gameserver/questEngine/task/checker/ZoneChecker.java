package com.aionemu.gameserver.questEngine.task.checker;

import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.world.zone.ZoneName;

/**
 * @author ATracer, Neon
 */
public class ZoneChecker extends DestinationChecker {

	protected final ZoneName zoneName;

	public ZoneChecker(Creature follower, ZoneName zoneName) {
		super(follower);
		this.zoneName = zoneName;
	}

	@Override
	public boolean check() {
		return follower.isInsideZone(zoneName);
	}
}
