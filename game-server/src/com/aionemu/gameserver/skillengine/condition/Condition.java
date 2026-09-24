package com.aionemu.gameserver.skillengine.condition;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.model.stats.calc.Stat2;
import com.aionemu.gameserver.model.stats.calc.StatCondition;
import com.aionemu.gameserver.model.stats.calc.functions.IStatFunction;
import com.aionemu.gameserver.skillengine.model.Effect;
import com.aionemu.gameserver.skillengine.model.Skill;

/**
 * @author ATracer
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Condition")
public abstract class Condition implements StatCondition {

	/**
	 * Validate condition specified in template
	 * 
	 * @param env
	 * @return true or false
	 */
	public abstract boolean validate(Skill env);

	/**
	 * Checks whether {@link #validate(Skill)} would succeed, without paying anything.
	 *
	 * @return True, if the condition is met
	 */
	public boolean canValidate(Skill skill) {
		return true;
	}

	@Override
	public boolean validate(Stat2 stat, IStatFunction statFunction) {
		return true;
	}

	public boolean validate(Effect effect) {
		return true;
	}

}
