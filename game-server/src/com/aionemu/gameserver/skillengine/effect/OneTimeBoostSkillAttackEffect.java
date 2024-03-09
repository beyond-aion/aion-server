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

		final float percent = 1.0f + value / 100.0f;
		switch (type) {
			case PHYSICAL, MAGICAL, ALL ->
				effect.addObserver(effect.getEffected(), new AttackCalcObserver() {

					private int boostCount = 0;

					@Override
					public float getBasePhysicalDamageMultiplier(boolean isSkill) {
						if (isSkill && type != SkillType.MAGICAL && boostCount++ < count) {
							if (boostCount == count)
								removeEffect(effect);
							return percent;
						}
						return 1.0f;
					}

					@Override
					public float getBaseMagicalDamageMultiplier() {
						if (type != SkillType.PHYSICAL && boostCount++ < count) {
							if (boostCount == count)
								removeEffect(effect);
							return percent;
						}
						return 1.0f;
					}
				});
		}
	}

	private void removeEffect(Effect effect) {
		ThreadPoolManager.getInstance().schedule(() -> effect.getEffected().getEffectController().removeEffect(effect.getSkillId()), 100);
	}

}
