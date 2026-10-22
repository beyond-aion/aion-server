package com.aionemu.gameserver.skillengine.effect;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.skillengine.model.Effect;

/**
 * @author kecimis
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "BuffSleepEffect")
public class BuffSleepEffect extends SleepEffect {

	@Override
	public void calculate(Effect effect) {
		effect.addSuccessEffect(this);
	}

	@Override
	public void startEffect(final Effect effect) {
		final Creature effected = effect.getEffected();
		effected.getController().cancelCurrentSkill(effect.getEffector());
		effect.setAbnormal(AbnormalState.SLEEP);
		effected.getEffectController().setAbnormal(AbnormalState.SLEEP);
	}

}
