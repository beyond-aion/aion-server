package com.aionemu.gameserver.skillengine.effect;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.skillengine.model.Effect;
import com.aionemu.gameserver.skillengine.model.SpellStatus;

/**
 * @author ATracer
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CloseAerialEffect")
public class CloseAerialEffect extends EffectTemplate {

	@Override
	public void applyEffect(Effect effect) {
		effect.getEffected().getEffectController().removeEffect(8224);
	}

	@Override
	public void calculate(Effect effect) {
		super.calculate(effect, null, SpellStatus.CLOSEAERIAL);
	}
}
