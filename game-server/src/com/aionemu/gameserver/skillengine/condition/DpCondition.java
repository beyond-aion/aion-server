package com.aionemu.gameserver.skillengine.condition;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.skillengine.model.Skill;

/**
 * @author ATracer
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DpCondition")
public class DpCondition extends Condition {

	@XmlAttribute(required = true)
	protected int value;

	@Override
	public boolean validate(Skill skill) {
		return ((Player) skill.getEffector()).getCommonData().getDp() >= value;
	}
}
