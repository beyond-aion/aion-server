package com.aionemu.gameserver.skillengine.effect;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.model.stats.container.StatEnum;
import com.aionemu.gameserver.skillengine.model.Effect;

/**
 * @author ATracer
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "BoostSkillCastingTimeEffect")
public class BoostSkillCastingTimeEffect extends BufEffect {

	@Override
	public void calculate(Effect effect) {
		if (effect.getEffected().isEnemy(effect.getEffector()) && value < 0) {
			super.calculate(effect, StatEnum.SLOW_RESISTANCE, null);
		} else {
			super.calculate(effect);
		}

	}

}
