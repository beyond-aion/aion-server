package com.aionemu.gameserver.skillengine.effect;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.controllers.observer.AttackCalcObserver;
import com.aionemu.gameserver.controllers.observer.AttackShieldObserver;
import com.aionemu.gameserver.skillengine.model.Effect;
import com.aionemu.gameserver.skillengine.model.ShieldType;

/**
 * @author ginho1
 * @modified Wakizashi, kecimis, Neon
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ReflectorEffect")
public class ReflectorEffect extends ShieldEffect {

	@XmlAttribute
	protected int reflectType;

	@Override
	public void startEffect(final Effect effect) {
		int hit = hitvalue + hitdelta * effect.getSkillLevel();

		AttackShieldObserver asObserver = new AttackShieldObserver(hit, value, percent, false, effect, hitType, getType(), hitTypeProb, minradius, radius,
			null, 0);

		effect.getEffected().getObserveController().addAttackCalcObserver(asObserver);
		effect.setAttackShieldObserver(asObserver, position);
	}

	@Override
	public void endEffect(Effect effect) {
		AttackCalcObserver acObserver = effect.getAttackShieldObserver(position);
		if (acObserver != null)
			effect.getEffected().getObserveController().removeAttackCalcObserver(acObserver);
	}

	@Override
	public ShieldType getType() {
		return reflectType == 1 ? ShieldType.SKILL_REFLECTOR : ShieldType.REFLECTOR;
	}
}
