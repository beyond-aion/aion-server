package com.aionemu.gameserver.skillengine.effect;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ATTACK_STATUS.LOG;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ATTACK_STATUS.TYPE;
import com.aionemu.gameserver.skillengine.model.Effect;
import com.aionemu.gameserver.skillengine.model.EffectReserved;
import com.aionemu.gameserver.skillengine.model.EffectReserved.ResourceType;
import com.aionemu.gameserver.skillengine.model.HealType;

/**
 * @author ATracer, Wakizashi, kecimis
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "AbstractHealEffect")
public abstract class AbstractHealEffect extends EffectTemplate implements HealEffectTemplate {

	@XmlAttribute
	protected boolean percent;

	public void calculate(Effect effect, HealType healType) {
		if (!super.calculate(effect, null, null))
			return;
		effect.setReserveds(new EffectReserved(position, calculateHealValue(effect, healType), ResourceType.of(healType), false), false);
	}

	public void applyEffect(Effect effect, HealType healType) {
		Creature effected = effect.getEffected();
		int healValue = effect.getReserveds(position).getValue();
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

	@Override
	public boolean isPercent() {
		return percent;
	}

	@Override
	public boolean allowHpHealBoost(Effect effect) {
		return !percent;
	}

	@Override
	public boolean allowHpHealSkillDeboost(Effect effect) {
		return true;
	}

	@Override
	public int calculateBaseHealValue(Effect effect) {
		return calculateBaseValue(effect);
	}

	@Override
	public int calculateHealValue(Effect effect, HealType type) {
		if (type == HealType.HP && effect.getEffected().getEffectController().isAbnormalSet(AbnormalState.DISEASE))
			return 0;
		int cap = getMaxStatValue(effect) - getCurrentStatValue(effect);
		int healValue = HealEffectTemplate.super.calculateHealValue(effect, type);
		return Math.min(cap, healValue);
	}
}
