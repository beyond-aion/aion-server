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
 * @Modified Majka (2015.07.13) - Added the attributes check_ok_dialog_id, check_fail_dialog_id to manage
 *           the new collecting quests in Cygnea, Enshar with quest script template.
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

	@XmlAttribute(name = "check_ok_dialog_id")
	protected int checkOkDialogId;

	@XmlAttribute(name = "check_fail_dialog_id")
	protected int checkFailDialogId;

	@Override
	public void register(QuestEngine questEngine) {
		ItemCollecting template = new ItemCollecting(id, startNpcIds, nextNpcId, endNpcIds, questMovie, startDialogId, startDialogId2, checkOkDialogId,
			checkFailDialogId);
		questEngine.addQuestHandler(template);
	}

}
