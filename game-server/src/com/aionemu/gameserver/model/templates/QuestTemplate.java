package com.aionemu.gameserver.model.templates;

import java.util.Collections;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlList;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.configs.main.CraftConfig;
import com.aionemu.gameserver.model.Gender;
import com.aionemu.gameserver.model.PlayerClass;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.templates.quest.CollectItems;
import com.aionemu.gameserver.model.templates.quest.InventoryItems;
import com.aionemu.gameserver.model.templates.quest.QuestBonuses;
import com.aionemu.gameserver.model.templates.quest.QuestCategory;
import com.aionemu.gameserver.model.templates.quest.QuestDrop;
import com.aionemu.gameserver.model.templates.quest.QuestExtraCategory;
import com.aionemu.gameserver.model.templates.quest.QuestItems;
import com.aionemu.gameserver.model.templates.quest.QuestKill;
import com.aionemu.gameserver.model.templates.quest.QuestMentorType;
import com.aionemu.gameserver.model.templates.quest.QuestRepeatCycle;
import com.aionemu.gameserver.model.templates.quest.QuestTarget;
import com.aionemu.gameserver.model.templates.quest.QuestWorkItems;
import com.aionemu.gameserver.model.templates.quest.Rewards;
import com.aionemu.gameserver.model.templates.quest.XMLStartCondition;

/**
 * @author MrPoke, vlog, Neon
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Quest")
public class QuestTemplate implements L10n {

	@XmlElement(name = "collect_items")
	private CollectItems collectItems;
	@XmlElement(name = "inventory_items")
	private InventoryItems inventoryItems;
	@XmlElement(name = "rewards")
	private List<Rewards> rewards;
	@XmlElement(name = "bonus")
	private QuestBonuses bonus;
	@XmlElement(name = "extended_rewards")
	private Rewards extendedRewards;
	@XmlElement(name = "quest_drop")
	private List<QuestDrop> questDrop;
	@XmlElement(name = "quest_kill")
	private List<QuestKill> questKill;
	@XmlElement(name = "start_conditions")
	private List<XMLStartCondition> startConds;
	@XmlList
	@XmlElement(name = "class_permitted")
	private List<PlayerClass> classPermitted;
	@XmlElement(name = "gender_permitted")
	private Gender genderPermitted;
	@XmlElement(name = "quest_work_items")
	private QuestWorkItems questWorkItems;
	@XmlElement(name = "fighter_selectable_reward")
	private List<QuestItems> fighterSelectableReward;
	@XmlElement(name = "knight_selectable_reward")
	private List<QuestItems> knightSelectableReward;
	@XmlElement(name = "ranger_selectable_reward")
	private List<QuestItems> rangerSelectableReward;
	@XmlElement(name = "assassin_selectable_reward")
	private List<QuestItems> assassinSelectableReward;
	@XmlElement(name = "wizard_selectable_reward")
	private List<QuestItems> wizardSelectableReward;
	@XmlElement(name = "elementalist_selectable_reward")
	private List<QuestItems> elementalistSelectableReward;
	@XmlElement(name = "priest_selectable_reward")
	private List<QuestItems> priestSelectableReward;
	@XmlElement(name = "chanter_selectable_reward")
	private List<QuestItems> chanterSelectableReward;
	@XmlElement(name = "gunner_selectable_reward")
	private List<QuestItems> gunnerSelectableReward;
	@XmlElement(name = "rider_selectable_reward")
	private List<QuestItems> riderSelectableReward;
	@XmlElement(name = "bard_selectable_reward")
	private List<QuestItems> bardSelectableReward;
	@XmlAttribute(name = "id", required = true)
	private int id;
	@XmlAttribute(name = "name")
	private String name;
	@XmlAttribute(name = "nameId")
	private int nameId;
	@XmlAttribute(name = "minlevel_permitted")
	private int minlevelPermitted;
	@XmlAttribute(name = "maxlevel_permitted")
	private int maxlevelPermitted;
	@XmlAttribute(name = "rank")
	private int rank;
	@XmlAttribute(name = "max_repeat_count")
	private int maxRepeatCount = 1;
	@XmlAttribute(name = "reward_repeat_count")
	private int rewardRepeatCount;
	@XmlAttribute(name = "cannot_share")
	private boolean cannotShare;
	@XmlAttribute(name = "cannot_giveup")
	private boolean cannotGiveup;
	@XmlAttribute(name = "can_report")
	private boolean canReport;
	@XmlAttribute(name = "use_class_reward")
	private int useClassReward;
	@XmlAttribute(name = "race_permitted")
	private Race racePermitted;
	@XmlAttribute(name = "combineskill")
	private int combineskill;
	@XmlAttribute(name = "combine_skillpoint")
	private int combineSkillpoint;
	@XmlAttribute(name = "timer")
	private boolean timer;
	@XmlAttribute(name = "category")
	private QuestCategory category = QuestCategory.QUEST;
	@XmlAttribute(name = "extra_category")
	private QuestExtraCategory extraCategory = QuestExtraCategory.NONE;
	@XmlAttribute(name = "repeat_cycle")
	private List<QuestRepeatCycle> repeatCycle;
	@XmlAttribute(name = "npcfaction_id")
	private int npcFactionId;
	@XmlAttribute(name = "mentor_type")
	private QuestMentorType mentorType = QuestMentorType.NONE;
	@XmlAttribute(name = "target")
	private QuestTarget target = QuestTarget.NONE;
	@XmlAttribute(name = "restricted")
	private boolean restricted = false;
	@XmlAttribute(name = "data_driven")
	private boolean dataDriven = false;

	/**
	 * Gets the value of the collectItems property.
	 * 
	 * @return possible object is {@link CollectItems }
	 */
	public CollectItems getCollectItems() {
		return collectItems;
	}

	public InventoryItems getInventoryItems() {
		return inventoryItems;
	}

	public List<Rewards> getRewards() {
		return rewards == null ? Collections.emptyList() : rewards;
	}

	public Rewards getExtendedRewards() {
		return extendedRewards;
	}

	public QuestBonuses getBonus() {
		return bonus;
	}

	public List<QuestDrop> getQuestDrop() {
		return questDrop == null ? Collections.emptyList() : questDrop;
	}

	public List<QuestKill> getQuestKill() {
		return questKill == null ? Collections.emptyList() : questKill;
	}

	public List<XMLStartCondition> getXMLStartConditions() {
		return startConds == null ? Collections.emptyList() : startConds;
	}

	public int getRequiredConditionCount() {
		if (getXMLStartConditions().isEmpty())
			return 0;
		int optionalCount = 0;
		int mandatoryCount = 0;
		for (XMLStartCondition cond : startConds) {
			if (cond.isOptional())
				optionalCount++;
			else
				mandatoryCount++;
		}

		int result = Math.min(1, optionalCount) + mandatoryCount;

		if (isMaster())
			result += 1 - CraftConfig.MAX_MASTER_CRAFTING_SKILLS;

		return result;
	}

	public List<PlayerClass> getClassPermitted() {
		return classPermitted == null ? Collections.emptyList() : classPermitted;
	}

	/**
	 * Gets the value of the genderPermitted property.
	 * 
	 * @return possible object is {@link Gender }
	 */
	public Gender getGenderPermitted() {
		return genderPermitted;
	}

	/**
	 * Gets the value of the questWorkItems property.
	 * 
	 * @return possible object is {@link QuestWorkItems }
	 */
	public QuestWorkItems getQuestWorkItems() {
		return questWorkItems;
	}

	public List<QuestItems> getSelectableRewardByClass(PlayerClass playerClass) {
		switch (playerClass) {
			case ASSASSIN:
				return assassinSelectableReward == null ? Collections.emptyList() : assassinSelectableReward;
			case CHANTER:
				return chanterSelectableReward == null ? Collections.emptyList() : chanterSelectableReward;
			case CLERIC:
				return priestSelectableReward == null ? Collections.emptyList() : priestSelectableReward;
			case GLADIATOR:
				return fighterSelectableReward == null ? Collections.emptyList() : fighterSelectableReward;
			case RANGER:
				return rangerSelectableReward == null ? Collections.emptyList() : rangerSelectableReward;
			case SORCERER:
				return wizardSelectableReward == null ? Collections.emptyList() : wizardSelectableReward;
			case SPIRIT_MASTER:
				return elementalistSelectableReward == null ? Collections.emptyList() : elementalistSelectableReward;
			case TEMPLAR:
				return knightSelectableReward == null ? Collections.emptyList() : knightSelectableReward;
			case GUNNER:
				return gunnerSelectableReward == null ? Collections.emptyList() : gunnerSelectableReward;
			case BARD:
				return bardSelectableReward == null ? Collections.emptyList() : bardSelectableReward;
			case RIDER:
				return riderSelectableReward == null ? Collections.emptyList() : riderSelectableReward;
		}
		return Collections.emptyList();
	}

	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	@Override
	public int getL10nId() {
		return nameId;
	}

	public int getMinlevelPermitted() {
		return minlevelPermitted;
	}

	public int getMaxlevelPermitted() {
		return maxlevelPermitted;
	}

	public int getRequiredRank() {
		return rank;
	}

	public int getMaxRepeatCount() {
		return maxRepeatCount;
	}

	public int getRewardRepeatCount() {
		return rewardRepeatCount;
	}

	public boolean isCannotShare() {
		return cannotShare;
	}

	public boolean isCannotGiveup() {
		return cannotGiveup;
	}

	public boolean isCanReport() {
		return canReport;
	}

	public boolean isClassRewardOnEveryRepeat() {
		return useClassReward == 1;
	}

	public boolean isSingleTimeClassReward() {
		return useClassReward == 2;
	}

	public boolean isRepeatable() {
		return getMaxRepeatCount() > 1;
	}

	public Race getRacePermitted() {
		return racePermitted;
	}

	public int getCombineSkill() {
		return combineskill;
	}

	public int getCombineSkillPoint() {
		return combineSkillpoint;
	}

	public boolean isTimer() {
		return timer;
	}

	public QuestCategory getCategory() {
		return category;
	}

	public QuestExtraCategory getExtraCategory() {
		return extraCategory;
	}

	public boolean isMentor() {
		return mentorType != QuestMentorType.NONE;
	}

	public QuestMentorType getMentorType() {
		return mentorType;
	}

	public QuestTarget getTarget() {
		return target;
	}

	public List<QuestRepeatCycle> getRepeatCycle() {
		return repeatCycle;
	}

	public int getNpcFactionId() {
		return npcFactionId;
	}

	public boolean isTimeBased() {
		return repeatCycle != null;
	}

	public boolean isDaily() {
		return isTimeBased() && repeatCycle.contains(QuestRepeatCycle.ALL);
	}

	public boolean isWeekly() {
		return isTimeBased() && !isDaily();
	}

	public boolean isMaster() {
		return getCombineSkillPoint() == 499;
	}

	public boolean isExpert() {
		return getCombineSkillPoint() == 399;
	}

	public boolean isProfession() {
		return isMaster() || isExpert();
	}

	/**
	 * @return True, if the quest is a mission quest (campaign quest)
	 */
	public boolean isMission() {
		return category == QuestCategory.MISSION;
	}

	public boolean isNoCount() {
		return category == QuestCategory.NON_COUNT || category == QuestCategory.EVENT;
	}

	/**
	 * @return the restricted, in client has any bm_restrict_category
	 */
	public boolean isRestricted() {
		return restricted;
	}

	/**
	 * @return the dataDriven property, if true quest setting are retrieved from quest_data_driven.xml file
	 */
	public boolean isDataDriven() {
		return dataDriven;
	}
}
