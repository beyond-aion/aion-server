package com.aionemu.gameserver.questEngine.handlers.models.xmlQuest.operations;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.questEngine.model.QuestEnv;

/**
 * @author Mr. Poke
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "StartQuestOperation")
public class StartQuestOperation extends QuestOperation {

	@XmlAttribute(required = true)
	protected int id;

	@Override
	public void doOperate(QuestEnv env) {
		// TODO Auto-generated method stub

	}
}
