package com.aionemu.gameserver.dataholders;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;

import com.aionemu.gameserver.questEngine.handlers.models.CraftingRewardsData;
import com.aionemu.gameserver.questEngine.handlers.models.FountainRewardsData;
import com.aionemu.gameserver.questEngine.handlers.models.ItemCollectingData;
import com.aionemu.gameserver.questEngine.handlers.models.ItemOrdersData;
import com.aionemu.gameserver.questEngine.handlers.models.KillInWorldData;
import com.aionemu.gameserver.questEngine.handlers.models.KillInZoneData;
import com.aionemu.gameserver.questEngine.handlers.models.KillSpawnedData;
import com.aionemu.gameserver.questEngine.handlers.models.MentorMonsterHuntData;
import com.aionemu.gameserver.questEngine.handlers.models.MonsterHuntData;
import com.aionemu.gameserver.questEngine.handlers.models.RelicRewardsData;
import com.aionemu.gameserver.questEngine.handlers.models.ReportToData;
import com.aionemu.gameserver.questEngine.handlers.models.ReportToManyData;
import com.aionemu.gameserver.questEngine.handlers.models.SkillUseData;
import com.aionemu.gameserver.questEngine.handlers.models.WorkOrdersData;
import com.aionemu.gameserver.questEngine.handlers.models.XMLQuest;
import com.aionemu.gameserver.questEngine.handlers.models.XmlQuestData;

/**
 * @author MrPoke
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "quest_scripts")
public class XMLQuests {

	@XmlElements({ @XmlElement(name = "report_to", type = ReportToData.class), @XmlElement(name = "monster_hunt", type = MonsterHuntData.class),
		@XmlElement(name = "xml_quest", type = XmlQuestData.class), @XmlElement(name = "item_collecting", type = ItemCollectingData.class),
		@XmlElement(name = "relic_rewards", type = RelicRewardsData.class), @XmlElement(name = "crafting_rewards", type = CraftingRewardsData.class),
		@XmlElement(name = "report_to_many", type = ReportToManyData.class), @XmlElement(name = "kill_in_world", type = KillInWorldData.class),
		@XmlElement(name = "kill_in_zone", type = KillInZoneData.class), @XmlElement(name = "skill_use", type = SkillUseData.class),
		@XmlElement(name = "kill_spawned", type = KillSpawnedData.class), @XmlElement(name = "mentor_monster_hunt", type = MentorMonsterHuntData.class),
		@XmlElement(name = "fountain_rewards", type = FountainRewardsData.class), @XmlElement(name = "item_order", type = ItemOrdersData.class),
		@XmlElement(name = "work_order", type = WorkOrdersData.class) })
	protected List<XMLQuest> data;

	/**
	 * @return the data
	 */
	public List<XMLQuest> getQuest() {
		return data;
	}

	/**
	 * @param data
	 *          the data to set
	 */
	public void setData(List<XMLQuest> data) {
		this.data = data;
	}
}
