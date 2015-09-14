package com.aionemu.gameserver.questEngine.task.checker;

import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.utils.MathUtil;

/**
 * @author ATracer
 * @modified Neon
 */
public class TargetDestinationChecker extends DestinationChecker {

	protected final Creature target;

	public TargetDestinationChecker(Creature follower, Creature target) {
		super(follower);
		this.target = target;
	}

	@Override
	public boolean check() {
		return MathUtil.isIn3dRange(target, follower, 20);
	}
}
