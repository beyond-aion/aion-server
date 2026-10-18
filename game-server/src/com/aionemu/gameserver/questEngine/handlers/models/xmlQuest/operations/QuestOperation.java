package com.aionemu.gameserver.questEngine.handlers.models.xmlQuest.operations;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlSeeAlso;
import jakarta.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.questEngine.model.QuestEnv;

/**
 * @author Mr. Poke
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "QuestOperation")
@XmlSeeAlso({ TakeItemOperation.class, StartQuestOperation.class, SetQuestVarOperation.class, NpcDialogOperation.class, GiveItemOperation.class,
	SetQuestStatusOperation.class })
public abstract class QuestOperation {

	public abstract void doOperate(QuestEnv env);
}
