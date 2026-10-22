package com.aionemu.gameserver.skillengine.condition;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.skillengine.model.Skill;

/**
 * @author Rolandas
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SkillChargeCondition")
public class SkillChargeCondition extends ChargeCondition {

	@Override
	public boolean validate(Skill env) {
		return true;
	}

	public int getValue() {
		return value;
	}
}
