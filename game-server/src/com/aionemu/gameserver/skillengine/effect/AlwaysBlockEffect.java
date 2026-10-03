package com.aionemu.gameserver.skillengine.effect;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.controllers.attack.AttackStatus;
import com.aionemu.gameserver.controllers.observer.AttackStatusObserver;
import com.aionemu.gameserver.skillengine.model.Effect;

/**
 * @author ATracer
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "AlwaysBlockEffect")
public class AlwaysBlockEffect extends EffectTemplate {

	@Override
	public void applyEffect(Effect effect) {
		effect.addToEffectedController();
	}

	@Override
	public void startEffect(Effect effect) {
		effect.addObserver(effect.getEffected(), new AttackStatusObserver(value, AttackStatus.BLOCK) {

			@Override
			public boolean checkStatus(AttackStatus status) {
				if (status == AttackStatus.BLOCK) {
					if (--value <= 0)
						effect.endEffect();
					return true;
				}
				return false;
			}

		});
	}
}
