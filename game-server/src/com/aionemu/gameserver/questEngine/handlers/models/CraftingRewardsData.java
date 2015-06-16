package com.aionemu.gameserver.questEngine.handlers.models;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.questEngine.QuestEngine;
import com.aionemu.gameserver.questEngine.handlers.template.CraftingRewards;

/**
 * @author Bobobear
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CraftingRewardsData")
public class CraftingRewardsData extends XMLQuest {

	@XmlAttribute(name = "start_npc_id", required = true)
	protected int startNpcId;
	@XmlAttribute(name = "end_npc_id")
	protected int endNpcId;
	@XmlAttribute(name = "skill_id")
	protected int skillId;
	@XmlAttribute(name = "level_reward")
	protected int levelReward;

	@Override
	public void register(QuestEngine questEngine) {
		CraftingRewards template = new CraftingRewards(id, startNpcId, skillId, levelReward, endNpcId, questMovie);
		questEngine.addQuestHandler(template);
	}

}
