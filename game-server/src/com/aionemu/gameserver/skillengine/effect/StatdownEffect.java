package com.aionemu.gameserver.skillengine.effect;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.skillengine.model.Effect;

/**
 * @author ATracer
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "StatdownEffect")
public class StatdownEffect extends BufEffect {

	@Override
	public void startEffect(Effect effect) {
		super.startEffect(effect);
		effect.getEffected().getLifeStats().updateCurrentStats();
	}

	// TODO bosses are resistent to this?
}
