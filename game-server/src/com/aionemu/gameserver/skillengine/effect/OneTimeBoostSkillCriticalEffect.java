package com.aionemu.gameserver.skillengine.effect;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.controllers.attack.AttackStatus;
import com.aionemu.gameserver.controllers.observer.AttackCalcObserver;
import com.aionemu.gameserver.controllers.observer.AttackerCriticalStatus;
import com.aionemu.gameserver.controllers.observer.AttackerCriticalStatusObserver;
import com.aionemu.gameserver.skillengine.model.Effect;

/**
 * @author Sippolo
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "OneTimeBoostSkillCriticalEffect")
public class OneTimeBoostSkillCriticalEffect extends EffectTemplate {

	@XmlAttribute
	private int count;
	@XmlAttribute
	private boolean percent;

	@Override
	public void applyEffect(Effect effect) {
		effect.addToEffectedController();
	}

	@Override
	public void startEffect(final Effect effect) {
		super.startEffect(effect);

		AttackerCriticalStatusObserver observer = new AttackerCriticalStatusObserver(AttackStatus.CRITICAL, count, value, percent) {

			@Override
			public AttackerCriticalStatus checkAttackerCriticalStatus(AttackStatus stat, boolean isSkill) {
				if (stat == this.status && isSkill) {
					if (this.getCount() <= 1)
						effect.endEffect();
					else
						this.decreaseCount();
						
					this.acStatus.setResult(true);
				}
				else
					this.acStatus.setResult(false);
				
				return this.acStatus;
			}
		};
		effect.getEffected().getObserveController().addAttackCalcObserver(observer);
		effect.setAttackStatusObserver(observer, position);
	}

	@Override
	public void endEffect(Effect effect) {
		super.endEffect(effect);

		AttackCalcObserver observer = effect.getAttackStatusObserver(position);
		effect.getEffected().getObserveController().removeAttackCalcObserver(observer);
	}
	
	/**
	 * @return the percent
	 */
	public boolean isPercent() {
		return percent;
	}
	
	
}
