package com.aionemu.gameserver.questEngine.handlers.models;

import java.util.List;

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
 * @author Mr. Poke
 * @modified Pad
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
}
