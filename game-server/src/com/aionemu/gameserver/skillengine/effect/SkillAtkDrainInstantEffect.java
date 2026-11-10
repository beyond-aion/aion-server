package com.aionemu.gameserver.skillengine.effect;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlType;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ATTACK_STATUS.LOG;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ATTACK_STATUS.TYPE;
import com.aionemu.gameserver.skillengine.model.Effect;
import com.aionemu.gameserver.utils.ThreadPoolManager;

/**
 * @author ATracer
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SkillAtkDrainInstantEffect")
public class SkillAtkDrainInstantEffect extends DamageEffect {

	@XmlAttribute(name = "hp_percent")
	private int hpPercent;
	@XmlAttribute(name = "mp_percent")
	private int mpPercent;
	/**
	 * Chance in percent that the caster gets the drain at all, plus prob_delta per skill level
	 */
	@XmlAttribute(name = "prob")
	private int prob = 100;
	@XmlAttribute(name = "prob_delta")
	private int probDelta;

	@Override
	public boolean appliesWhenEffectedIsDead() {
		return true;
	}

	@Override
	public void applyEffect(Effect effect) {
		if (effect.getEffected() != null && !effect.getEffected().isDead()) // a dead target can no longer take the damage, but the caster keeps the drain that the client already displayed
			super.applyEffect(effect);
		if (Rnd.chance() >= prob + probDelta * effect.getSkillLevel())
			return;
		ThreadPoolManager.getInstance().schedule(() -> {
			if (hpPercent != 0) {
				effect.getEffector().getLifeStats().increaseHp(TYPE.ABSORBED_HP, effect.getReserveds(position).getValue() * hpPercent / 100, effect,
						LOG.SKILLLATKDRAININSTANT);
			}
			if (mpPercent != 0) {
				effect.getEffector().getLifeStats().increaseMp(TYPE.MP, effect.getReserveds(position).getValue() * mpPercent / 100, effect.getSkillId(),
						LOG.SKILLLATKDRAININSTANT);
			}
		}, 1000); // 1000 ms after the hit; the target's death must not cancel it (see appliesWhenEffectedIsDead)
	}
}
