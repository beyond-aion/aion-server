package com.aionemu.gameserver.questEngine.handlers.models;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.questEngine.QuestEngine;
import com.aionemu.gameserver.questEngine.handlers.template.ReportTo;

/**
 * @author MrPoke
 * @modified Pad
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ReportToData")
public class ReportToData extends XMLQuest {

	@XmlAttribute(name = "start_npc_ids")
	protected List<Integer> startNpcIds;

	@XmlAttribute(name = "end_npc_ids")
	protected List<Integer> endNpcIds;

	@XmlAttribute(name = "start_dialog_id")
	protected int startDialog = 0;

	@XmlAttribute(name = "end_dialog_id")
	protected int endDialog = 0;
	
	@Override
	public void register(QuestEngine questEngine) {
		ReportTo template = new ReportTo(id, startNpcIds, endNpcIds, startDialog, endDialog);
		questEngine.addQuestHandler(template);
	}
}
