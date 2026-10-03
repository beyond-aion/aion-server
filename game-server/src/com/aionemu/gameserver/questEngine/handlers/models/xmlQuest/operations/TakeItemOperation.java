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
@XmlType(name = "TakeItemOperation")
public class TakeItemOperation extends QuestOperation {

	@XmlAttribute(name = "item_id", required = true)
	protected int itemId;
	@XmlAttribute(required = true)
	protected int count;

	@Override
	public void doOperate(QuestEnv env) {
		env.getPlayer().getInventory().decreaseByItemId(itemId, count);
	}
}
