package com.aionemu.gameserver.questEngine.handlers.models.xmlQuest.operations;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

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
