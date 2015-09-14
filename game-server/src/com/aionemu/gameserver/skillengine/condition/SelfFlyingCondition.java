package com.aionemu.gameserver.skillengine.condition;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.skillengine.model.Effect;
import com.aionemu.gameserver.skillengine.model.FlyingRestriction;
import com.aionemu.gameserver.skillengine.model.Skill;

/**
 * @author kecimis
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SelfFlyingCondition")
public class SelfFlyingCondition extends Condition {

	@XmlAttribute(required = true)
	protected FlyingRestriction restriction;

	@Override
	public boolean validate(Skill env) {
		if (env.getEffector() == null)
			return false;

		switch (restriction) {
			case FLY:
				return env.getEffector().isInFlyingState();
			case GROUND:
				return !env.getEffector().isInFlyingState();
		}

		return true;
	}

	@Override
	public boolean validate(Effect effect) {
		if (effect.getEffector() == null)
			return false;

		switch (restriction) {
			case FLY:
				return effect.getEffector().isInFlyingState();
			case GROUND:
				return !effect.getEffector().isInFlyingState();
		}

		return true;
	}

}
