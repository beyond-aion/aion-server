package com.aionemu.gameserver.skillengine.effect;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.skillengine.model.Effect;
import com.aionemu.gameserver.skillengine.model.HealType;

/**
 * @author ATracer
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "FPHealInstantEffect")
public class FPHealInstantEffect extends AbstractHealEffect {

	@Override
	public void calculate(Effect effect) {
		super.calculate(effect, HealType.FP);
	}

	@Override
	public void applyEffect(Effect effect) {
		super.applyEffect(effect, HealType.FP);
	}

	@Override
	public int getCurrentStatValue(Effect effect) {
		return effect.getEffected().getLifeStats().getCurrentFp();
	}

	@Override
	public int getMaxStatValue(Effect effect) {
		return effect.getEffected().getLifeStats().getMaxFp();
	}

}
