package com.aionemu.gameserver.skillengine.condition;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.skillengine.model.Skill;

/**
 * @author ATracer
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PlayerMovedCondition")
public class PlayerMovedCondition extends Condition {

	@XmlAttribute(required = true)
	protected boolean allow;

	/**
	 * Gets the value of the allow property.
	 */
	public boolean isAllow() {
		return allow;
	}

	@Override
	public boolean validate(Skill skill) {
		return allow == skill.getConditionChangeListener().isEffectorMoved();
	}
}
