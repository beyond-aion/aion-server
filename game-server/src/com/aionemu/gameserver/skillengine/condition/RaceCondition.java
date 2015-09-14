package com.aionemu.gameserver.skillengine.condition;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.skillengine.model.Effect;
import com.aionemu.gameserver.skillengine.model.Skill;

/**
 * @author kecimis
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "RaceCondition")
public class RaceCondition extends Condition {

	@XmlAttribute(name = "race")
	private List<Race> races;

	/*
	 * (non-Javadoc)
	 * @see com.aionemu.gameserver.skillengine.condition.Condition#validate(com.aionemu.gameserver.skillengine.model.Skill)
	 */
	@Override
	public boolean validate(Skill env) {
		if (env.getFirstTarget() == null || env.getEffector() == null)
			return false;
		if (!(env.getFirstTarget() instanceof Creature))
			return false;

		boolean result = false;
		for (Race race : races) {
			if (race == env.getFirstTarget().getRace())
				result = true;
		}

		return result;
	}

	@Override
	public boolean validate(Effect effect) {
		if (effect.getEffected() == null || effect.getEffector() == null)
			return false;
		if (!(effect.getEffected() instanceof Creature))
			return false;

		boolean result = false;
		for (Race race : races) {
			if (race == effect.getEffected().getRace())
				result = true;
		}

		return result;
	}

}
