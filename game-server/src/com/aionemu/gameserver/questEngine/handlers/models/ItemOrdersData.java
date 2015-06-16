package com.aionemu.gameserver.questEngine.handlers.models;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.questEngine.QuestEngine;
import com.aionemu.gameserver.questEngine.handlers.template.ItemOrders;

/**
 * @author Bobobear
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ItemOrdersData")
public class ItemOrdersData extends XMLQuest {

	@XmlAttribute(name = "talk_npc_id1")
	protected int talkNpc1;
	@XmlAttribute(name = "talk_npc_id2")
	protected int talkNpc2;
	@XmlAttribute(name = "end_npc_id", required = true)
	protected int endNpcId;

	@Override
	public void register(QuestEngine questEngine) {
		ItemOrders template = new ItemOrders(id, talkNpc1, talkNpc2, endNpcId);
		questEngine.addQuestHandler(template);
	}
}
