package com.aionemu.gameserver.questEngine.handlers.models.xmlQuest;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.questEngine.handlers.models.xmlQuest.conditions.QuestConditions;
import com.aionemu.gameserver.questEngine.handlers.models.xmlQuest.operations.QuestOperations;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;

/**
 * @author Mr. Poke
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DialogAction", propOrder = { "conditions", "operations" })
public class QuestDialog {

	@XmlElement(name = "conditions")
	protected QuestConditions conditions;
	
	@XmlElement(name = "operations")
	protected QuestOperations operations;
	
	@XmlAttribute(required = true)
	protected int id;

	public boolean operate(QuestEnv env, QuestState qs) {
		if (env.getDialogActionId() != id)
			return false;
		if (conditions == null || conditions.checkConditionOfSet(env)) {
			if (operations != null) {
				return operations.operate(env);
			}
		}
		return false;
	}
}
