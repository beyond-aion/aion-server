package com.aionemu.gameserver.skillengine.effect;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.model.stats.container.StatEnum;
import com.aionemu.gameserver.skillengine.model.Effect;

/**
 * @author ATracer
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CurseEffect")
public class CurseEffect extends BufEffect {

	@Override
	public void calculate(Effect effect) {
		super.calculate(effect, StatEnum.CURSE_RESISTANCE, null);
	}

	@Override
	public void startEffect(Effect effect) {
		super.startEffect(effect);
		effect.setAbnormal(AbnormalState.CURSE);
		effect.getEffected().getEffectController().setAbnormal(AbnormalState.CURSE);
	}

	@Override
	public void endEffect(Effect effect) {
		super.endEffect(effect);
		effect.getEffected().getEffectController().unsetAbnormal(AbnormalState.CURSE);
	}
}
