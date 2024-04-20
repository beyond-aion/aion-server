package com.aionemu.gameserver.questEngine.handlers.models;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.questEngine.QuestEngine;
import com.aionemu.gameserver.questEngine.handlers.template.ReportToMany;

/**
 * @author Hilgert, Rolandas, Pad
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ReportToManyData", propOrder = { "npcInfos" })
public class ReportToManyData extends XMLQuest {

	@XmlElement(name = "npc_infos", required = true)
	private List<NpcInfos> npcInfos;

	@XmlAttribute(name = "start_item_id")
	private int startItemId;

	@XmlAttribute(name = "start_npc_ids")
	private List<Integer> startNpcIds;

	@XmlAttribute(name = "start_dialog_id")
	private int startDialogId;

	@Override
	public void register(QuestEngine questEngine) {
		questEngine.addQuestHandler(new ReportToMany(id, startItemId, startNpcIds, npcInfos, startDialogId, mission));
	}

	@Override
	public Set<Integer> getAlternativeNpcs(int npcId) {
		if (startNpcIds != null && startNpcIds.size() > 1 && startNpcIds.contains(npcId))
			return startNpcIds.stream().filter(id -> id != npcId).collect(Collectors.toSet());
		for (NpcInfos npcInfo : npcInfos) {
			List<Integer> npcIds = npcInfo.getNpcIds();
			if (npcIds.size() > 1 && npcIds.contains(npcId))
				return npcIds.stream().filter(id -> id != npcId).collect(Collectors.toSet());
		}
		return null;
	}
}
