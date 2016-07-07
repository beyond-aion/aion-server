package com.aionemu.gameserver.questEngine.handlers.models.xmlQuest.operations;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.services.item.ItemService;

/**
 * @author Mr. Poke
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "GiveItemOperation")
public class GiveItemOperation extends QuestOperation {

	@XmlAttribute(name = "item_id", required = true)
	protected int itemId;
	@XmlAttribute(required = true)
	protected int count;

	@Override
	public void doOperate(QuestEnv env) {
		ItemService.addItem(env.getPlayer(), itemId, count, true);
	}
}
