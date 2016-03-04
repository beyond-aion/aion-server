package com.aionemu.gameserver.skillengine.effect;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.model.stats.container.StatEnum;
import com.aionemu.gameserver.skillengine.model.Effect;

/**
 * @author ATracer, kecimis
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ConfuseEffect")
public class ConfuseEffect extends EffectTemplate {

	@Override
	public void applyEffect(Effect effect) {
		effect.addToEffectedController();
	}

	@Override
	public void calculate(Effect effect) {
		super.calculate(effect, StatEnum.CONFUSE_RESISTANCE, null);
	}

	@Override
	public void startEffect(Effect effect) {
		effect.getEffected().getEffectController().setAbnormal(AbnormalState.CONFUSE.getId());
		effect.setAbnormal(AbnormalState.CONFUSE.getId());
		//TODO implement move events (similar to fear)
	}

	@Override
	public void endEffect(Effect effect) {
		effect.getEffected().getEffectController().unsetAbnormal(AbnormalState.CONFUSE.getId());
	}

}
