package com.aionemu.gameserver.skillengine.effect;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.controllers.observer.AttackShieldObserver;
import com.aionemu.gameserver.skillengine.model.Effect;
import com.aionemu.gameserver.skillengine.model.ShieldType;

/**
 * @author Cheatkiller
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "MPShieldEffect")
public class MPShieldEffect extends ShieldEffect {

	@XmlAttribute(name = "mp_value")
	protected int mpValue;

	@Override
	public void startEffect(Effect effect) {
		int valueWithDelta = calculateBaseValue(effect);
		int hitValueWithDelta = hitvalue + hitdelta * effect.getSkillLevel();
		AttackShieldObserver asObserver = new AttackShieldObserver(hitValueWithDelta, valueWithDelta, percent, effect, hitType, getType(), hitTypeProb,
			mpValue);
		effect.addObserver(effect.getEffected(), asObserver);
	}

	@Override
	public void endEffect(Effect effect) {
	}

	@Override
	public ShieldType getType() {
		return ShieldType.MPSHIELD;
	}
}
