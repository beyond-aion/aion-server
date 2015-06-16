package com.aionemu.gameserver.skillengine.effect;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.controllers.observer.ActionObserver;
import com.aionemu.gameserver.controllers.observer.AttackCalcObserver;
import com.aionemu.gameserver.controllers.observer.AttackShieldObserver;
import com.aionemu.gameserver.controllers.observer.ObserverType;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Summon;
import com.aionemu.gameserver.skillengine.model.Effect;
import com.aionemu.gameserver.skillengine.model.ShieldType;

/**
 * @author Sippolo modified by kecimis
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ProtectEffect")
public class ProtectEffect extends ShieldEffect {

	@Override
	public void startEffect(final Effect effect) {
		AttackShieldObserver asObserver = new AttackShieldObserver(value, this.hitvalue,
			radius, percent, effect, this.hitType, this.getType(), this.hitTypeProb);
		
		effect.getEffected().getObserveController().addAttackCalcObserver(asObserver);
		effect.setAttackShieldObserver(asObserver, position);
		
		if (effect.getEffector() instanceof Summon) {
			ActionObserver summonRelease = new ActionObserver(ObserverType.SUMMONRELEASE) {

				@Override
				public void summonrelease() {
					effect.endEffect();
				}

			};
			effect.getEffector().getObserveController().attach(summonRelease);
			effect.setActionObserver(summonRelease, position);
		}
		else {
			ActionObserver death = new ActionObserver(ObserverType.DEATH) {

				@Override
				public void died(Creature creature) {
					effect.endEffect();
				}
			
			};
			effect.getEffector().getObserveController().attach(death);
			effect.setActionObserver(death, position);
		}
			
	}

	@Override
	public void endEffect(Effect effect) {
		AttackCalcObserver acObserver = effect.getAttackShieldObserver(position);
		if (acObserver != null)
			effect.getEffected().getObserveController().removeAttackCalcObserver(acObserver);
		ActionObserver aObserver = effect.getActionObserver(position);
		if (aObserver != null)
			effect.getEffector().getObserveController().removeObserver(aObserver);
	}
	
	@Override
	public ShieldType getType() {
		return ShieldType.PROTECT;
	}
}
