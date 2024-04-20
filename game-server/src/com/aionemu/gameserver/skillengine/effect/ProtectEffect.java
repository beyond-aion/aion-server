package com.aionemu.gameserver.skillengine.effect;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.controllers.observer.ActionObserver;
import com.aionemu.gameserver.controllers.observer.AttackShieldObserver;
import com.aionemu.gameserver.controllers.observer.ObserverType;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Summon;
import com.aionemu.gameserver.skillengine.model.Effect;
import com.aionemu.gameserver.skillengine.model.ShieldType;

/**
 * @author Sippolo, kecimis
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ProtectEffect")
public class ProtectEffect extends ShieldEffect {

	@Override
	public void startEffect(final Effect effect) {
		AttackShieldObserver asObserver = new AttackShieldObserver(value, hitvalue, percent, false, effect, hitType, getType(), hitTypeProb, 0, radius,
			null, 0);
		effect.addObserver(effect.getEffected(), asObserver);

		if (effect.getEffector() instanceof Summon) {
			effect.addObserver(effect.getEffector(), new ActionObserver(ObserverType.SUMMONRELEASE) {

				@Override
				public void summonrelease() {
					effect.endEffect();
				}

			});
		} else {
			effect.addObserver(effect.getEffector(), new ActionObserver(ObserverType.DEATH) {

				@Override
				public void died(Creature creature) {
					effect.endEffect();
				}

			});
		}
	}

	@Override
	public void endEffect(Effect effect) {
	}

	@Override
	public ShieldType getType() {
		return ShieldType.PROTECT;
	}
}
