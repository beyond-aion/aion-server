package com.aionemu.gameserver.questEngine.handlers.models;

import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.questEngine.QuestEngine;
import com.aionemu.gameserver.questEngine.handlers.template.ItemOrders;

/**
 * @author Bobobear, Pad
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ItemOrdersData")
public class ItemOrdersData extends XMLQuest {

	@XmlAttribute(name = "talk_npc_id1")
	protected int talkNpcId1;

	@XmlAttribute(name = "talk_npc_id2")
	protected int talkNpcId2;

	@XmlAttribute(name = "end_npc_id")
	protected int endNpcId;

	@Override
	public void register(QuestEngine questEngine) {
		questEngine.addQuestHandler(new ItemOrders(id, talkNpcId1, talkNpcId2, endNpcId));
	}

	@Override
	public Set<Integer> getAlternativeNpcs(int npcId) {
		return null;
	}
}
