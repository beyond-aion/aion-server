package com.aionemu.gameserver.questEngine.handlers.models.xmlQuest.conditions;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.questEngine.model.QuestEnv;

/**
 * @author Mr. Poke
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DialogIdCondition")
public class DialogIdCondition extends QuestCondition {

	@XmlAttribute(required = true)
	protected int value;

	/**
	 * Gets the value of the value property.
	 */
	public int getValue() {
		return value;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.aionemu.gameserver.questEngine.handlers.template.xmlQuest.condition.QuestCondition#doCheck(com.aionemu.gameserver
	 * .questEngine.model.QuestEnv)
	 */
	@Override
	public boolean doCheck(QuestEnv env) {
		int data = env.getDialogId();
		switch (getOp()) {
			case EQUAL:
				return data == value;
			case NOT_EQUAL:
				return data != value;
			default:
				return false;
		}
	}
}
