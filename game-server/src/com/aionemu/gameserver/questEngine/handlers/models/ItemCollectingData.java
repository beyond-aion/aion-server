package com.aionemu.gameserver.questEngine.handlers.models;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.questEngine.QuestEngine;
import com.aionemu.gameserver.questEngine.handlers.template.ItemCollecting;

/**
 * @author MrPoke, Rolandas, Majka, Pad
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ItemCollectingData")
public class ItemCollectingData extends XMLQuest {

	@XmlAttribute(name = "start_npc_ids")
	protected List<Integer> startNpcIds;

	@XmlAttribute(name = "end_npc_ids")
	protected List<Integer> endNpcIds;

	@XmlAttribute(name = "next_npc_id")
	protected int nextNpcId;

	@XmlAttribute(name = "start_zone")
	protected String startZone;

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
		questEngine.addQuestHandler(new ItemCollecting(id, startNpcIds, nextNpcId, endNpcIds, startZone, questMovie, startDialogId, startDialogId2,
			checkOkDialogId, checkFailDialogId));
	}

	@Override
	public Set<Integer> getAlternativeNpcs(int npcId) {
		if (startNpcIds != null && startNpcIds.size() > 1 && startNpcIds.contains(npcId))
			return startNpcIds.stream().filter(id -> id != npcId).collect(Collectors.toSet());
		if (endNpcIds != null && endNpcIds.size() > 1 && endNpcIds.contains(npcId))
			return endNpcIds.stream().filter(id -> id != npcId).collect(Collectors.toSet());
		return null;
	}
}
