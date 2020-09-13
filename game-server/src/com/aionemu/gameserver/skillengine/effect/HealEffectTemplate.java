package com.aionemu.gameserver.skillengine.effect;

import com.aionemu.gameserver.model.stats.container.StatEnum;
import com.aionemu.gameserver.skillengine.model.Effect;
import com.aionemu.gameserver.skillengine.model.HealType;

/**
 * @author Neon
 */
public interface HealEffectTemplate {

	boolean isPercent();
	boolean allowHpHealBoost(Effect effect);
	boolean allowHpHealSkillDeboost(Effect effect);
	int getCurrentStatValue(Effect effect);
	int getMaxStatValue(Effect effect);
	int calculateBaseHealValue(Effect effect);

	default int calculateHealValue(Effect effect, HealType type) {
		int healValue = isPercent() ? getMaxStatValue(effect) * calculateBaseHealValue(effect) / 100 : calculateBaseHealValue(effect);

		if (type == HealType.HP) {
			if (allowHpHealBoost(effect)) {
				// caster's heal boost from equipment, titles, etc. (capped at 1000 / 100% boost)
				int healBoost = effect.getEffector().getGameStats().getStat(StatEnum.HEAL_BOOST, 0).getCurrent();
				// caster's heal related effects (passive boosts, active buffs e.g. blessed shield)
				int healSkillBoost = effect.getEffector().getGameStats().getStat(StatEnum.HEAL_SKILL_BOOST, 1000).getCurrent() - 1000;
				healValue += Math.round(healValue * (healBoost + healSkillBoost) / 1000f);
			}
			// apply target's heal related effects (e.g. brilliant protection)
			if (allowHpHealSkillDeboost(effect))
				healValue = effect.getEffected().getGameStats().getStat(StatEnum.HEAL_SKILL_DEBOOST, healValue).getCurrent();
		}
		return healValue;
	}
}
