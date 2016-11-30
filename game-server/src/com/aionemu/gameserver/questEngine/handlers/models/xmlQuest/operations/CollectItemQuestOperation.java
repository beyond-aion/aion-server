package com.aionemu.gameserver.questEngine.handlers.models.xmlQuest.operations;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.services.QuestService;

/**
 * @author Mr. Poke
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CollectItemQuestOperation", propOrder = { "_true", "_false" })
public class CollectItemQuestOperation extends QuestOperation {

	@XmlElement(name = "true", required = true)
	protected QuestOperations _true;
	@XmlElement(name = "false", required = true)
	protected QuestOperations _false;
	@XmlAttribute(required = true)
	protected Boolean removeItems;

	@Override
	public void doOperate(QuestEnv env) {
		if (QuestService.collectItemCheck(env, removeItems == null ? true : false))
			_true.operate(env);
		else
			_false.operate(env);
	}

}
