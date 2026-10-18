package com.aionemu.gameserver.skillengine.effect;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.skillengine.model.Effect;
import com.aionemu.gameserver.skillengine.model.HealType;

/**
 * @author kecimis
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "MPHealEffect")
public class MPHealEffect extends HealOverTimeEffect {

	@Override
	public void startEffect(Effect effect) {
		super.startEffect(effect, HealType.MP);
	}

	@Override
	public void onPeriodicAction(Effect effect) {
		super.onPeriodicAction(effect, HealType.MP);
	}

	@Override
	public int getCurrentStatValue(Effect effect) {
		return effect.getEffected().getLifeStats().getCurrentMp();
	}

	@Override
	public int getMaxStatValue(Effect effect) {
		return effect.getEffected().getGameStats().getMaxMp().getCurrent();
	}
}
