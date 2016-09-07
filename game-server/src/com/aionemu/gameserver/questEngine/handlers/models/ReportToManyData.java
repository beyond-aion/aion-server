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

import javolution.util.FastMap;

/**
 * @author Hilgert
 * @modified Rolandas, Pad
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ReportToManyData", propOrder = { "npcInfos" })
public class ReportToManyData extends XMLQuest {

	@XmlElement(name = "npc_infos", required = true)
	protected List<NpcInfos> npcInfos;
	
	@XmlAttribute(name = "start_item_id")
	protected int startItemId;

	@XmlAttribute(name = "start_npc_ids")
	protected List<Integer> startNpcIds;

	@XmlAttribute(name = "end_npc_ids")
	protected List<Integer> endNpcIds;

	@XmlAttribute(name = "start_dialog_id")
	protected int startDialogId;

	@XmlAttribute(name = "end_dialog_id")
	protected int endDialogId;

	@Override
	public void register(QuestEngine questEngine) {
		int maxVar = -1;
		FastMap<List<Integer>, NpcInfos> npcInfo = new FastMap<>();
		for (NpcInfos nI : npcInfos) {
			npcInfo.put(nI.getNpcIds(), nI);
			maxVar++;
		}
		questEngine.addQuestHandler(new ReportToMany(id, startItemId, startNpcIds, endNpcIds, npcInfo, startDialogId, endDialogId, maxVar, mission));
	}

	@Override
	public Set<Integer> getAlternativeNpcs(int npcId) {
		if (startNpcIds != null && startNpcIds.size() > 1 && startNpcIds.contains(npcId))
			return startNpcIds.stream().filter(id -> id != npcId).collect(Collectors.toSet());
		if (endNpcIds != null && endNpcIds.size() > 1 && endNpcIds.contains(npcId))
			return endNpcIds.stream().filter(id -> id != npcId).collect(Collectors.toSet());
		for (NpcInfos npcInfo : npcInfos) {
			List<Integer> npcIds = npcInfo.getNpcIds();
			if (npcIds != null && npcIds.size() > 1 && npcIds.contains(npcId))
				return npcIds.stream().filter(id -> id != npcId).collect(Collectors.toSet());
		}
		return null;
	}
}
