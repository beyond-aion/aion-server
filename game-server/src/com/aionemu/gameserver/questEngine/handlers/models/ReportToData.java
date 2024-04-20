package com.aionemu.gameserver.questEngine.handlers.models;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.questEngine.QuestEngine;
import com.aionemu.gameserver.questEngine.handlers.template.ReportTo;

/**
 * @author MrPoke, Pad, Neon
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ReportToData")
public class ReportToData extends XMLQuest {

	@XmlAttribute(name = "start_npc_ids")
	private List<Integer> startNpcIds;

	@XmlAttribute(name = "end_npc_ids")
	private List<Integer> endNpcIds;

	@XmlAttribute(name = "start_dialog_id")
	private int startDialogId;

	@Override
	public void register(QuestEngine questEngine) {
		questEngine.addQuestHandler(new ReportTo(id, startNpcIds, endNpcIds, startDialogId));
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
