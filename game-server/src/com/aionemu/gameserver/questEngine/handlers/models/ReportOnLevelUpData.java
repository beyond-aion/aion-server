package com.aionemu.gameserver.questEngine.handlers.models;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.questEngine.QuestEngine;
import com.aionemu.gameserver.questEngine.handlers.template.ReportOnLevelUp;

/**
 * @author Majka, Bobobear
 * @modified Pad
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ReportOnLevelUpData")
public class ReportOnLevelUpData extends XMLQuest {

	@XmlAttribute(name = "end_npc_ids")
	protected List<Integer> endNpcIds;

	@Override
	public void register(QuestEngine questEngine) {
		questEngine.addQuestHandler(new ReportOnLevelUp(id, endNpcIds));
	}
}
