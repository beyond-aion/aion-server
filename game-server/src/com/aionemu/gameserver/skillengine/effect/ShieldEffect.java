package com.aionemu.gameserver.skillengine.effect;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.controllers.observer.AttackShieldObserver;
import com.aionemu.gameserver.skillengine.model.Effect;
import com.aionemu.gameserver.skillengine.model.ShieldType;

/**
 * @author ATracer modified by Wakizashi, Sippolo, kecimis
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ShieldEffect")
public class ShieldEffect extends EffectTemplate {

	@XmlAttribute
	protected int hitdelta;
	@XmlAttribute
	protected int hitvalue;
	@XmlAttribute
	protected boolean percent;
	@XmlAttribute
	protected int radius = 0;
	@XmlAttribute
	protected int minradius = 0;

	@Override
	public void applyEffect(Effect effect) {
		// check for condition race, skillId: 10317,10318, implemented as RaceCondition
		effect.addToEffectedController();
	}

	@Override
	public void startEffect(Effect effect) {
		int valueWithDelta = calculateBaseValue(effect);
		int hitValueWithDelta = hitvalue + hitdelta * effect.getSkillLevel();

		AttackShieldObserver asObserver = new AttackShieldObserver(hitValueWithDelta, valueWithDelta, percent, effect, hitType, getType(), hitTypeProb);
		effect.addObserver(effect.getEffected(), asObserver);
		effect.getEffected().getEffectController().setUnderShield(true);
	}

	@Override
	public void endEffect(Effect effect) {
		effect.getEffected().getEffectController().setUnderShield(false);
	}

	public ShieldType getType() {
		return ShieldType.NORMAL;
	}

}
