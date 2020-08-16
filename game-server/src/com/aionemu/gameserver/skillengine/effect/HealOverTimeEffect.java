package com.aionemu.gameserver.skillengine.effect;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.stats.container.StatEnum;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ATTACK_STATUS.LOG;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ATTACK_STATUS.TYPE;
import com.aionemu.gameserver.skillengine.model.Effect;
import com.aionemu.gameserver.skillengine.model.EffectReserved;
import com.aionemu.gameserver.skillengine.model.EffectReserved.ResourceType;
import com.aionemu.gameserver.skillengine.model.HealType;

/**
 * @author ATracer
 * @author kecimis
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "HealOverTimeEffect")
public abstract class HealOverTimeEffect extends AbstractOverTimeEffect {

	@Override
	public void calculate(Effect effect) {
		if (!super.calculate(effect, null, null))
			return;

		effect.addSuccessEffect(this);
	}

	public void startEffect(Effect effect, HealType healType) {
		// calculate value of heals
		Creature effector = effect.getEffector();
		Creature effected = effect.getEffected();
		int valueWithDelta = calculateBaseValue(effect);
		int maxCurValue = getMaxStatValue(effect);
		int possibleHealValue = 0;
		if (percent)
			possibleHealValue = maxCurValue * valueWithDelta / 100;
		else
			possibleHealValue = valueWithDelta;

		int finalHeal = possibleHealValue;

		if (healType == HealType.HP) {
			if (effect.getItemTemplate() == null) {
				int healBoost = effector.getGameStats().getStat(StatEnum.HEAL_BOOST, 0).getCurrent(); // capped by 100%
				// Apply caster's heal related effects (passive boosts, active buffs e.g. blessed shield)
				int healSkillBoost = effector.getGameStats().getStat(StatEnum.HEAL_SKILL_BOOST, 1000).getCurrent() - 1000;
				finalHeal += Math.round(finalHeal * (healBoost + healSkillBoost) / 1000f); 
			}
			// Apply target's heal related effects (e.g. brilliant protection)
			finalHeal = effected.getGameStats().getStat(StatEnum.HEAL_SKILL_DEBOOST, finalHeal).getCurrent();
		}
		effect.setReserveds(new EffectReserved(position, finalHeal, ResourceType.of(healType), false, false), true);

		super.startEffect(effect, null);
	}

	public void onPeriodicAction(Effect effect, HealType healType) {
		Creature effected = effect.getEffected();

		int currentValue = getCurrentStatValue(effect);
		int maxCurValue = getMaxStatValue(effect);
		int possibleHealValue = effect.getReserveds(position).getValue();

		int healValue = maxCurValue - currentValue < possibleHealValue ? (maxCurValue - currentValue) : possibleHealValue;

		if (healValue <= 0)
			return;

		switch (healType) {
			case HP:
				effected.getLifeStats().increaseHp(TYPE.HP, healValue, effect, LOG.HEAL);
				break;
			case MP:
				effected.getLifeStats().increaseMp(TYPE.MP, healValue, effect.getSkillId(), LOG.MPHEAL);
				break;
			case FP:
				((Player) effected).getLifeStats().increaseFp(TYPE.FP, healValue, effect.getSkillId(), LOG.FPHEAL);
				break;
			case DP:
				((Player) effected).getCommonData().addDp(healValue);
				break;
		}

	}

	protected abstract int getCurrentStatValue(Effect effect);

	protected abstract int getMaxStatValue(Effect effect);
}
