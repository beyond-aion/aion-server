package com.aionemu.gameserver.skillengine.effect;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.network.aion.serverpackets.SM_ATTACK_STATUS.LOG;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ATTACK_STATUS.TYPE;
import com.aionemu.gameserver.skillengine.model.Effect;

/**
 * @author ATracer
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SkillAtkDrainInstantEffect")
public class SkillAtkDrainInstantEffect extends DamageEffect {

	@XmlAttribute(name = "hp_percent")
	protected int hp_percent;
	@XmlAttribute(name = "mp_percent")
	protected int mp_percent;

	@Override
	public void applyEffect(Effect effect) {
		super.applyEffect(effect);
		if (hp_percent != 0) {
			effect
				.getEffector()
				.getLifeStats()
				.increaseHp(TYPE.ABSORBED_HP, effect.getReserveds(this.position).getValue() * hp_percent / 100, effect.getSkillId(),
					LOG.SKILLLATKDRAININSTANT);
		}
		if (mp_percent != 0) {
			effect.getEffector().getLifeStats()
				.increaseMp(TYPE.MP, effect.getReserveds(this.position).getValue() * mp_percent / 100, effect.getSkillId(), LOG.SKILLLATKDRAININSTANT);
		}
	}
}
