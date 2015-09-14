package com.aionemu.gameserver.skillengine.condition;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.model.stats.calc.Stat2;
import com.aionemu.gameserver.model.stats.calc.functions.IStatFunction;
import com.aionemu.gameserver.skillengine.model.Effect;
import com.aionemu.gameserver.skillengine.model.Skill;

/**
 * @author ATracer
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "OnFlyCondition")
public class OnFlyCondition extends Condition {

	@Override
	public boolean validate(Skill env) {
		return env.getEffector().isFlying();
	}

	@Override
	public boolean validate(Stat2 stat, IStatFunction statFunction) {
		return stat.getOwner().isFlying();
	}

	@Override
	public boolean validate(Effect effect) {
		return effect.getEffected().isFlying();
	}
}
