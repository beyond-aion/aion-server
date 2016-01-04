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
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ReportOnLevelUpData")
public class ReportOnLevelUpData extends XMLQuest {

	@XmlAttribute(name = "end_npc_ids")
	protected List<Integer> endNpcIds;

	@XmlAttribute(name = "end_dialog_id")
	protected int endDialog = 2375;

	@Override
	public void register(QuestEngine questEngine) {
		ReportOnLevelUp template = new ReportOnLevelUp(id, endNpcIds, endDialog);
		questEngine.addQuestHandler(template);
	}
}
