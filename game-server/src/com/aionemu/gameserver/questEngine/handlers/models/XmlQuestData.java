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
import com.aionemu.gameserver.questEngine.handlers.models.xmlQuest.events.OnKillEvent;
import com.aionemu.gameserver.questEngine.handlers.models.xmlQuest.events.OnTalkEvent;
import com.aionemu.gameserver.questEngine.handlers.template.XmlQuest;

/**
 * @author Mr. Poke, Pad
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "XmlQuest", propOrder = { "onTalkEvents", "onKillEvents" })
public class XmlQuestData extends XMLQuest {

	@XmlElement(name = "on_talk_event")
	protected List<OnTalkEvent> onTalkEvents;

	@XmlElement(name = "on_kill_event")
	protected List<OnKillEvent> onKillEvents;

	@XmlAttribute(name = "start_npc_ids")
	protected List<Integer> startNpcIds;

	@XmlAttribute(name = "end_npc_ids")
	protected List<Integer> endNpcIds;

	@Override
	public void register(QuestEngine questEngine) {
		questEngine.addQuestHandler(new XmlQuest(id, startNpcIds, endNpcIds, onTalkEvents, onKillEvents));
	}

	@Override
	public Set<Integer> getAlternativeNpcs(int npcId) {
		if (startNpcIds != null && startNpcIds.size() > 1 && startNpcIds.contains(npcId))
			return startNpcIds.stream().filter(id -> id != npcId).collect(Collectors.toSet());
		if (endNpcIds != null && endNpcIds.size() > 1 && endNpcIds.contains(npcId))
			return endNpcIds.stream().filter(id -> id != npcId).collect(Collectors.toSet());
		if (onTalkEvents != null) {
			for (OnTalkEvent onTalkEvent : onTalkEvents) {
				List<Integer> npcIds = onTalkEvent.getIds();
				if (npcIds.size() > 1 && npcIds.contains(npcId))
					return npcIds.stream().filter(id -> id != npcId).collect(Collectors.toSet());
			}
		}
		if (onKillEvents != null) {
			for (OnKillEvent onKillEvent : onKillEvents) {
				for (Monster monster : onKillEvent.getMonsters()) {
					List<Integer> npcIds = monster.getNpcIds();
					if (npcIds.size() > 1 && npcIds.contains(npcId))
						return npcIds.stream().filter(id -> id != npcId).collect(Collectors.toSet());
				}
			}
		}
		return null;
	}
}
