package com.aionemu.gameserver.skillengine.effect;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.controllers.observer.AttackCalcObserver;
import com.aionemu.gameserver.skillengine.model.Effect;
import com.aionemu.gameserver.skillengine.model.SkillType;
import com.aionemu.gameserver.utils.ThreadPoolManager;

/**
 * @author ATracer
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "OneTimeBoostSkillAttackEffect")
public class OneTimeBoostSkillAttackEffect extends BufEffect {

	@XmlAttribute
	private int count;

	@XmlAttribute
	private SkillType type;

	@Override
	public void startEffect(final Effect effect) {
		super.startEffect(effect);

		final int stopCount = count;
		final float percent = 1.0f + value / 100.0f;
		AttackCalcObserver observer = null;

		switch (type) {
			case MAGICAL:
				observer = new AttackCalcObserver() {

					private int count = 0;

					@Override
					public float getBaseMagicalDamageMultiplier() {
						if (count++ < stopCount)
							return percent;
						else {
							ThreadPoolManager.getInstance().schedule(new Runnable() {

								@Override
								public void run() {
									effect.getEffected().getEffectController().removeEffect(effect.getSkillId());
								}
							}, 100);
						}
						return 1.0f;
					}
				};
				break;
			case PHYSICAL:
				observer = new AttackCalcObserver() {

					private int count = 0;

					@Override
					public float getBasePhysicalDamageMultiplier(boolean isSkill) {
						if (!isSkill)
							return 1f;

						if (count++ < stopCount) {
							if (count == stopCount) {
								ThreadPoolManager.getInstance().schedule(new Runnable() {

									@Override
									public void run() {
										effect.getEffected().getEffectController().removeEffect(effect.getSkillId());
									}
								}, 100);
							}
							return percent;
						}
						return 1.0f;
					}
				};
				break;
			case ALL:
				observer = new AttackCalcObserver() {

					private int count = 0;

					@Override
					public float getBasePhysicalDamageMultiplier(boolean isSkill) {
						if (!isSkill)
							return 1f;

						if (count++ < stopCount) {
							if (count == stopCount) {
								ThreadPoolManager.getInstance().schedule(new Runnable() {

									@Override
									public void run() {
										effect.getEffected().getEffectController().removeEffect(effect.getSkillId());
									}
								}, 100);
							}
							return percent;
						}
						return 1.0f;
					}
					@Override
					public float getBaseMagicalDamageMultiplier() {
						if (count++ < stopCount)
							return percent;
						else {
							ThreadPoolManager.getInstance().schedule(new Runnable() {

								@Override
								public void run() {
									effect.getEffected().getEffectController().removeEffect(effect.getSkillId());
								}
							}, 100);
						}
						return 1.0f;
					}
				};
				break;
		}

		effect.getEffected().getObserveController().addAttackCalcObserver(observer);
		effect.setAttackStatusObserver(observer, position);
	}

	@Override
	public void endEffect(Effect effect) {
		super.endEffect(effect);
		AttackCalcObserver observer = effect.getAttackStatusObserver(position);
		effect.getEffected().getObserveController().removeAttackCalcObserver(observer);
	}
}
