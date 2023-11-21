package com.aionemu.gameserver.dataholders;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.*;

import com.aionemu.gameserver.questEngine.handlers.models.*;

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
		@XmlElement(name = "work_order", type = WorkOrdersData.class), @XmlElement(name = "report_on_levelup", type = ReportOnLevelUpData.class) })
	private List<XMLQuest> data;

	@XmlTransient
	private final Map<Integer, XMLQuest> questsById = new HashMap<>();

	void afterUnmarshal(Unmarshaller u, Object parent) {
		if (data != null) {
			questsById.clear();
			data.forEach(quest -> questsById.put(quest.getId(), quest));
			data = null;
		}
	}

	public Collection<XMLQuest> getAllQuests() {
		return questsById.values();
	}

	public XMLQuest getQuest(int questId) {
		return questsById.get(questId);
	}

	public void setData(List<XMLQuest> data) {
		this.data = data;
		afterUnmarshal(null, null);
	}
}
