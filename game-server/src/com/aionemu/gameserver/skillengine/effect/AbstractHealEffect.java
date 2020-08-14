package com.aionemu.gameserver.skillengine.effect;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
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
 * @author ATracer modified by Wakizashi, kecimis
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "AbstractHealEffect")
public abstract class AbstractHealEffect extends EffectTemplate {

	@XmlAttribute
	protected boolean percent;

	public void calculate(Effect effect, HealType healType) {
		if (!super.calculate(effect, null, null))
			return;

		int valueWithDelta = calculateBaseValue(effect);
		int finalHeal = calculateHeal(effect, healType, valueWithDelta, getCurrentStatValue(effect), getMaxStatValue(effect));

		effect.setReserveds(new EffectReserved(position, finalHeal, ResourceType.of(healType), false), false);
	}

	public int calculateHeal(Effect effect, HealType type, int valueWithDelta, int currentValue, int maxCurValue) {
		Creature effector = effect.getEffector();
		Creature effected = effect.getEffected();

		int finalHeal = percent ? maxCurValue * valueWithDelta / 100 : valueWithDelta;

		if (type == HealType.HP) {
			if (!(this instanceof ProcHealInstantEffect || (this instanceof HealInstantEffect && percent))) {
				int healBoost = effector.getGameStats().getStat(StatEnum.HEAL_BOOST, 0).getCurrent();
				finalHeal += Math.round(finalHeal * healBoost / 1000f); // capped by 100%
				// Apply caster's heal related effects (passive boosts, active buffs e.g. blessed shield)
				finalHeal = effector.getGameStats().getStat(StatEnum.HEAL_SKILL_BOOST, finalHeal).getCurrent();
			}
			// Apply target's heal related effects (e.g. brilliant protection)
			finalHeal = effected.getGameStats().getStat(StatEnum.HEAL_SKILL_DEBOOST, finalHeal).getCurrent();
		}

		if (type == HealType.HP && effected.getEffectController().isAbnormalSet(AbnormalState.DISEASE))
			finalHeal = 0;
		else
			finalHeal = maxCurValue - currentValue < finalHeal ? (maxCurValue - currentValue) : finalHeal;

		return finalHeal;
	}

	public void applyEffect(Effect effect, HealType healType) {
		Creature effected = effect.getEffected();
		int healValue = effect.getReserveds(position).getValue();

		if (healValue <= 0)
			return;

		switch (healType) {
			case HP:
				if (this instanceof ProcHealInstantEffect)// item heal, eg potions
					effected.getLifeStats().increaseHp(TYPE.HP, healValue, effect.getEffector());
				else
					effected.getLifeStats().increaseHp(TYPE.REGULAR, healValue, effect.getEffector());
				break;
			case MP:
				if (this instanceof ProcMPHealInstantEffect)// item heal, eg potions
					effected.getLifeStats().increaseMp(TYPE.MP, healValue, 0, LOG.REGULAR);
				else
					effected.getLifeStats().increaseMp(TYPE.HEAL_MP, healValue, 0, LOG.REGULAR);
				break;
			case FP:
				if (!(effected instanceof Player))
					return;
				((Player) effected).getLifeStats().increaseFp(TYPE.FP_RINGS, healValue, 0, LOG.REGULAR);
				break;
			case DP:
				((Player) effected).getCommonData().addDp(healValue);
				break;
		}
	}

	protected abstract int getCurrentStatValue(Effect effect);

	protected abstract int getMaxStatValue(Effect effect);
}
