package com.aionemu.gameserver.questEngine.handlers.models;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.questEngine.QuestEngine;
import com.aionemu.gameserver.questEngine.handlers.template.ItemCollecting;

/**
 * @author MrPoke
 * @modified Rolandas
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ItemCollectingData")
public class ItemCollectingData extends XMLQuest {

	@XmlAttribute(name = "start_npc_ids", required = true)
	protected List<Integer> startNpcIds;

	@XmlAttribute(name = "end_npc_ids")
	protected List<Integer> endNpcIds;
	
	@XmlAttribute(name = "next_npc_id", required = true)
	protected int nextNpcId;
	
	@XmlAttribute(name = "start_dialog_id")
	protected int startDialogId;
	
	@XmlAttribute(name = "start_dialog_id2")
	protected int startDialogId2;
	
	@Override
	public void register(QuestEngine questEngine) {
		ItemCollecting template = new ItemCollecting(id, startNpcIds, nextNpcId, endNpcIds, questMovie, startDialogId, startDialogId2);
		questEngine.addQuestHandler(template);
	}

}
