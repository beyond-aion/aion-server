package com.aionemu.gameserver.questEngine.handlers.models;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.questEngine.QuestEngine;
import com.aionemu.gameserver.questEngine.handlers.template.FountainRewards;

/**
 * @author Bobobear
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "FountainRewardsData")
public class FountainRewardsData extends XMLQuest {

	@XmlAttribute(name = "start_npc_ids", required = true)
	protected List<Integer> startNpcIds;

	@Override
	public void register(QuestEngine questEngine) {
		FountainRewards template = new FountainRewards(id, startNpcIds);
		questEngine.addQuestHandler(template);
	}
}
