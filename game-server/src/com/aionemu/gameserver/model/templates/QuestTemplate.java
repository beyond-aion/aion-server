package com.aionemu.gameserver.model.templates;

import java.util.ArrayList;
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
 * @author MrPoke
 * @modified vlog, Neon
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Quest"/*
												 * , propOrder = { "collectItems", "rewards", "questDrop", "startConds", "classPermitted", "genderPermitted",
												 * "questWorkItems", "fighterSelectableReward", "knightSelectableReward", "rangerSelectableReward",
												 * "assassinSelectableReward", "wizardSelectableReward", "elementalistSelectableReward", "priestSelectableReward",
												 * "chanterSelectableReward" }
												 */)
public class QuestTemplate {

	@XmlElement(name = "collect_items")
	private CollectItems collectItems;
	@XmlElement(name = "inventory_items")
	private InventoryItems inventoryItems;
	@XmlElement(name = "rewards")
	private List<Rewards> rewards;
	@XmlElement(name = "bonus")
	private List<QuestBonuses> bonus;
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

	public QuestTemplate() {
		rewards = new ArrayList<>();
		bonus = new ArrayList<>();
		questDrop = new ArrayList<>();
		questKill = new ArrayList<>();
		startConds = new ArrayList<>();
		classPermitted = new ArrayList<>();
		fighterSelectableReward = new ArrayList<>();
		knightSelectableReward = new ArrayList<>();
		rangerSelectableReward = new ArrayList<>();
		assassinSelectableReward = new ArrayList<>();
		wizardSelectableReward = new ArrayList<>();
		elementalistSelectableReward = new ArrayList<>();
		priestSelectableReward = new ArrayList<>();
		chanterSelectableReward = new ArrayList<>();
		gunnerSelectableReward = new ArrayList<>();
		bardSelectableReward = new ArrayList<>();
		riderSelectableReward = new ArrayList<>();
	}

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

	/**
	 * Gets the value of the rewards property.
	 * <p>
	 * This accessor method returns a reference to the live list, not a snapshot. Therefore any modification you make to the returned list will be
	 * present inside the JAXB object. This is why there is not a <CODE>set</CODE> method for the rewards property.
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getRewards().add(newItem);
	 * </pre>
	 * <p>
	 * Objects of the following type(s) are allowed in the list {@link Rewards }
	 */
	public List<Rewards> getRewards() {
		return this.rewards;
	}

	public Rewards getExtendedRewards() {
		return this.extendedRewards;
	}

	public List<QuestBonuses> getBonus() {
		return this.bonus;
	}

	/**
	 * Gets the value of the questDrop property.
	 * <p>
	 * This accessor method returns a reference to the live list, not a snapshot. Therefore any modification you make to the returned list will be
	 * present inside the JAXB object. This is why there is not a <CODE>set</CODE> method for the questDrop property.
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getQuestDrop().add(newItem);
	 * </pre>
	 * <p>
	 * Objects of the following type(s) are allowed in the list {@link QuestDrop }
	 */
	public List<QuestDrop> getQuestDrop() {
		return this.questDrop;
	}

	public List<QuestKill> getQuestKill() {
		return this.questKill;
	}

	public List<XMLStartCondition> getXMLStartConditions() {
		return startConds;
	}

	public int getRequiredConditionCount() {
		if (getXMLStartConditions().size() == 0)
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

		if (this.isMaster())
			result += 1 - CraftConfig.MAX_MASTER_CRAFTING_SKILLS;

		return result;
	}

	/**
	 * Gets the value of the classPermitted property.
	 * <p>
	 * This accessor method returns a reference to the live list, not a snapshot. Therefore any modification you make to the returned list will be
	 * present inside the JAXB object. This is why there is not a <CODE>set</CODE> method for the classPermitted property.
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getClassPermitted().add(newItem);
	 * </pre>
	 * <p>
	 * Objects of the following type(s) are allowed in the list {@link PlayerClass }
	 */
	public List<PlayerClass> getClassPermitted() {
		return this.classPermitted;
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
				return assassinSelectableReward;
			case CHANTER:
				return chanterSelectableReward;
			case CLERIC:
				return priestSelectableReward;
			case GLADIATOR:
				return fighterSelectableReward;
			case RANGER:
				return rangerSelectableReward;
			case SORCERER:
				return wizardSelectableReward;
			case SPIRIT_MASTER:
				return elementalistSelectableReward;
			case TEMPLAR:
				return knightSelectableReward;
			case GUNNER:
				return gunnerSelectableReward;
			case BARD:
				return bardSelectableReward;
			case RIDER:
				return riderSelectableReward;
		}
		return null;
	}

	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public int getNameId() {
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
