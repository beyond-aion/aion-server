package com.aionemu.gameserver.skillengine.effect;

import com.aionemu.gameserver.skillengine.model.Effect;

/**
 * An effect which hits after a delay counted from the end of the cast. As the first effect of a skill it holds back the other effects until it
 * hits.
 */
public interface DelayedAttackEffect {

	/**
	 * @return The delay for the given skill level, counted from the end of the cast.
	 */
	int getDelay(int skillLevel);

	static int calculateDelay(int delay, int delayDelta, int skillLevel) {
		int delayWithDelta = delay + delayDelta * skillLevel;
		return delayWithDelta < 0 ? 500 : delayWithDelta;
	}

	/**
	 * @return The part of the delay which has not yet passed when the effects of the skill are applied.
	 */
	default int getRemainingDelay(Effect effect) {
		int delay = getDelay(effect.getSkillLevel());
		return effect.getSkill() == null ? delay : Math.max(0, delay - effect.getSkill().getLandingDelay());
	}
}
