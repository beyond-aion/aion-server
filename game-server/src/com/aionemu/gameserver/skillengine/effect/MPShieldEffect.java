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
 * @author Cheatkiller
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "MPShieldEffect")
public class MPShieldEffect extends ShieldEffect {
	
	@XmlAttribute(name = "mp_value")
	protected int mpValue;
	
	@Override
	public void startEffect(final Effect effect) {
		int skillLvl = effect.getSkillLevel();
		int valueWithDelta = value + delta * skillLvl;
		int hitValueWithDelta = hitvalue + hitdelta * skillLvl;
		AttackShieldObserver asObserver = new AttackShieldObserver(hitValueWithDelta, valueWithDelta, percent,
			effect, hitType, this.getType(), this.hitTypeProb, this.mpValue);

		effect.getEffected().getObserveController().addAttackCalcObserver(asObserver);
		effect.setAttackShieldObserver(asObserver, position);
		effect.getEffected().getEffectController().setUnderShield(true);
	}

	@Override
	public void endEffect(Effect effect) {
		AttackCalcObserver acObserver = effect.getAttackShieldObserver(position);
		if (acObserver != null)
			effect.getEffected().getObserveController().removeAttackCalcObserver(acObserver);
		effect.getEffected().getEffectController().setUnderShield(false);
	}
	
	@Override
	public ShieldType getType() {
		return ShieldType.MPSHIELD;
	}
}
	
