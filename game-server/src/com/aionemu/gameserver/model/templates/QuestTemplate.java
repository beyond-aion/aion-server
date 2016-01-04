package com.aionemu.gameserver.model.templates;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlList;
import javax.xml.bind.annotation.XmlType;

import javolution.util.FastTable;

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
 * @modified vlog
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
	protected CollectItems collectItems;
	@XmlElement(name = "inventory_items")
	protected InventoryItems inventoryItems;
	@XmlElement(name = "rewards")
	protected List<Rewards> rewards;
	@XmlElement(name = "bonus")
	protected List<QuestBonuses> bonus;
	@XmlElement(name = "extended_rewards")
	protected List<Rewards> extendedRewards;
	@XmlElement(name = "quest_drop")
	protected List<QuestDrop> questDrop;
	@XmlElement(name = "quest_kill")
	protected List<QuestKill> questKill;
	@XmlElement(name = "start_conditions")
	protected List<XMLStartCondition> startConds;
	@XmlList
	@XmlElement(name = "class_permitted")
	protected List<PlayerClass> classPermitted;
	@XmlElement(name = "gender_permitted")
	protected Gender genderPermitted;
	@XmlElement(name = "quest_work_items")
	protected QuestWorkItems questWorkItems;
	@XmlElement(name = "fighter_selectable_reward")
	protected List<QuestItems> fighterSelectableReward;
	@XmlElement(name = "knight_selectable_reward")
	protected List<QuestItems> knightSelectableReward;
	@XmlElement(name = "ranger_selectable_reward")
	protected List<QuestItems> rangerSelectableReward;
	@XmlElement(name = "assassin_selectable_reward")
	protected List<QuestItems> assassinSelectableReward;
	@XmlElement(name = "wizard_selectable_reward")
	protected List<QuestItems> wizardSelectableReward;
	@XmlElement(name = "elementalist_selectable_reward")
	protected List<QuestItems> elementalistSelectableReward;
	@XmlElement(name = "priest_selectable_reward")
	protected List<QuestItems> priestSelectableReward;
	@XmlElement(name = "chanter_selectable_reward")
	protected List<QuestItems> chanterSelectableReward;
	@XmlElement(name = "gunner_selectable_reward")
	protected List<QuestItems> gunnerSelectableReward;
	@XmlElement(name = "rider_selectable_reward")
	protected List<QuestItems> riderSelectableReward;
	@XmlElement(name = "bard_selectable_reward")
	protected List<QuestItems> bardSelectableReward;
	@XmlAttribute(name = "id", required = true)
	protected int id;
	@XmlAttribute(name = "name")
	protected String name;
	@XmlAttribute(name = "nameId")
	protected Integer nameId;
	@XmlAttribute(name = "minlevel_permitted")
	protected Integer minlevelPermitted;
	@XmlAttribute(name = "maxlevel_permitted")
	protected int maxlevelPermitted;
	@XmlAttribute(name = "rank")
	private int rank;
	@XmlAttribute(name = "max_repeat_count")
	protected Integer maxRepeatCount;
	@XmlAttribute(name = "reward_repeat_count")
	protected Integer rewardRepeatCount;
	@XmlAttribute(name = "cannot_share")
	protected Boolean cannotShare;
	@XmlAttribute(name = "cannot_giveup")
	protected Boolean cannotGiveup;
	@XmlAttribute(name = "can_report")
	protected Boolean canReport;
	@XmlAttribute(name = "use_class_reward")
	protected Integer useClassReward;
	@XmlAttribute(name = "race_permitted")
	protected Race racePermitted;
	@XmlAttribute(name = "combineskill")
	protected Integer combineskill;
	@XmlAttribute(name = "combine_skillpoint")
	protected Integer combineSkillpoint;
	@XmlAttribute(name = "timer")
	protected Boolean timer;
	@XmlAttribute(name = "category")
	protected QuestCategory category;
	@XmlAttribute(name = "extra_category")
	protected QuestExtraCategory extraCategory;
	@XmlAttribute(name = "repeat_cycle")
	protected List<QuestRepeatCycle> repeatCycle;
	@XmlAttribute(name = "npcfaction_id")
	protected int npcFactionId;
	@XmlAttribute(name = "mentor_type")
	protected QuestMentorType mentorType = QuestMentorType.NONE;
	@XmlAttribute(name = "target")
	private QuestTarget target = QuestTarget.NONE;
	@XmlAttribute(name = "restricted")
	private boolean restricted = false;
	@XmlAttribute(name = "data_driven")
	private boolean dataDriven = false;

	public QuestTemplate() {
		rewards = new FastTable<>();
		bonus = new FastTable<>();
		extendedRewards = new FastTable<>();
		questDrop = new FastTable<>();
		questKill = new FastTable<>();
		startConds = new FastTable<>();
		classPermitted = new FastTable<>();
		fighterSelectableReward = new FastTable<>();
		knightSelectableReward = new FastTable<>();
		rangerSelectableReward = new FastTable<>();
		assassinSelectableReward = new FastTable<>();
		wizardSelectableReward = new FastTable<>();
		elementalistSelectableReward = new FastTable<>();
		priestSelectableReward = new FastTable<>();
		chanterSelectableReward = new FastTable<>();
		gunnerSelectableReward = new FastTable<>();
		bardSelectableReward = new FastTable<>();
		riderSelectableReward = new FastTable<>();
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

	public List<Rewards> getExtendedRewards() {
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

	/**
	 * Gets the value of the fighterSelectableReward property.
	 * <p>
	 * This accessor method returns a reference to the live list, not a snapshot. Therefore any modification you make to the returned list will be
	 * present inside the JAXB object. This is why there is not a <CODE>set</CODE> method for the fighterSelectableReward property.
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getFighterSelectableReward().add(newItem);
	 * </pre>
	 * <p>
	 * Objects of the following type(s) are allowed in the list {@link QuestItems }
	 */
	public List<QuestItems> getFighterSelectableReward() {
		return this.fighterSelectableReward;
	}

	/**
	 * Gets the value of the knightSelectableReward property.
	 * <p>
	 * This accessor method returns a reference to the live list, not a snapshot. Therefore any modification you make to the returned list will be
	 * present inside the JAXB object. This is why there is not a <CODE>set</CODE> method for the knightSelectableReward property.
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getKnightSelectableReward().add(newItem);
	 * </pre>
	 * <p>
	 * Objects of the following type(s) are allowed in the list {@link QuestItems }
	 */
	public List<QuestItems> getKnightSelectableReward() {
		return this.knightSelectableReward;
	}

	/**
	 * Gets the value of the rangerSelectableReward property.
	 * <p>
	 * This accessor method returns a reference to the live list, not a snapshot. Therefore any modification you make to the returned list will be
	 * present inside the JAXB object. This is why there is not a <CODE>set</CODE> method for the rangerSelectableReward property.
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getRangerSelectableReward().add(newItem);
	 * </pre>
	 * <p>
	 * Objects of the following type(s) are allowed in the list {@link QuestItems }
	 */
	public List<QuestItems> getRangerSelectableReward() {
		return this.rangerSelectableReward;
	}

	/**
	 * Gets the value of the assassinSelectableReward property.
	 * <p>
	 * This accessor method returns a reference to the live list, not a snapshot. Therefore any modification you make to the returned list will be
	 * present inside the JAXB object. This is why there is not a <CODE>set</CODE> method for the assassinSelectableReward property.
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getAssassinSelectableReward().add(newItem);
	 * </pre>
	 * <p>
	 * Objects of the following type(s) are allowed in the list {@link QuestItems }
	 */
	public List<QuestItems> getAssassinSelectableReward() {
		return this.assassinSelectableReward;
	}

	/**
	 * Gets the value of the wizardSelectableReward property.
	 * <p>
	 * This accessor method returns a reference to the live list, not a snapshot. Therefore any modification you make to the returned list will be
	 * present inside the JAXB object. This is why there is not a <CODE>set</CODE> method for the wizardSelectableReward property.
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getWizardSelectableReward().add(newItem);
	 * </pre>
	 * <p>
	 * Objects of the following type(s) are allowed in the list {@link QuestItems }
	 */
	public List<QuestItems> getWizardSelectableReward() {
		return this.wizardSelectableReward;
	}

	/**
	 * Gets the value of the elementalistSelectableReward property.
	 * <p>
	 * This accessor method returns a reference to the live list, not a snapshot. Therefore any modification you make to the returned list will be
	 * present inside the JAXB object. This is why there is not a <CODE>set</CODE> method for the elementalistSelectableReward property.
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getElementalistSelectableReward().add(newItem);
	 * </pre>
	 * <p>
	 * Objects of the following type(s) are allowed in the list {@link QuestItems }
	 */
	public List<QuestItems> getElementalistSelectableReward() {
		return this.elementalistSelectableReward;
	}

	/**
	 * Gets the value of the priestSelectableReward property.
	 * <p>
	 * This accessor method returns a reference to the live list, not a snapshot. Therefore any modification you make to the returned list will be
	 * present inside the JAXB object. This is why there is not a <CODE>set</CODE> method for the priestSelectableReward property.
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getPriestSelectableReward().add(newItem);
	 * </pre>
	 * <p>
	 * Objects of the following type(s) are allowed in the list {@link QuestItems }
	 */
	public List<QuestItems> getPriestSelectableReward() {
		return this.priestSelectableReward;
	}

	/**
	 * Gets the value of the chanterSelectableReward property.
	 * <p>
	 * This accessor method returns a reference to the live list, not a snapshot. Therefore any modification you make to the returned list will be
	 * present inside the JAXB object. This is why there is not a <CODE>set</CODE> method for the chanterSelectableReward property.
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getChanterSelectableReward().add(newItem);
	 * </pre>
	 * <p>
	 * Objects of the following type(s) are allowed in the list {@link QuestItems }
	 */
	public List<QuestItems> getChanterSelectableReward() {
		return this.chanterSelectableReward;
	}

	public List<QuestItems> getGunnerSelectableReward() {
		return this.gunnerSelectableReward;
	}

	public List<QuestItems> getBardSelectableReward() {
		return this.bardSelectableReward;
	}

	public List<QuestItems> getRiderSelectableReward() {
		return this.riderSelectableReward;
	}

	public List<QuestItems> getSelectableRewardByClass(PlayerClass playerClass) {
		switch (playerClass) {
			case ASSASSIN: {
				return this.assassinSelectableReward;
			}
			case CHANTER: {
				return this.chanterSelectableReward;
			}
			case CLERIC: {
				return this.priestSelectableReward;
			}
			case GLADIATOR: {
				return this.fighterSelectableReward;
			}
			case RANGER: {
				return this.rangerSelectableReward;
			}
			case SORCERER: {
				return this.wizardSelectableReward;
			}
			case SPIRIT_MASTER: {
				return this.elementalistSelectableReward;
			}
			case TEMPLAR: {
				return this.knightSelectableReward;
			}
			case GUNNER: {
				return this.gunnerSelectableReward;
			}
			case BARD: {
				return this.bardSelectableReward;
			}
			case RIDER: {
				return this.riderSelectableReward;
			}
		}
		return null;
	}

	/**
	 * Gets the value of the id property.
	 */
	public int getId() {
		return id;
	}

	/**
	 * Gets the value of the name property.
	 * 
	 * @return possible object is {@link String }
	 */
	public String getName() {
		return name;
	}

	/**
	 * Gets the value of the nameId property.
	 * 
	 * @return possible object is {@link Integer }
	 */
	public Integer getNameId() {
		return nameId;
	}

	/**
	 * Gets the value of the minlevelPermitted property.
	 * 
	 * @return possible object is {@link Integer }
	 */
	public Integer getMinlevelPermitted() {
		return minlevelPermitted;
	}

	public int getMaxlevelPermitted() {
		return maxlevelPermitted;
	}

	public int getRequiredRank() {
		return rank;
	}

	/**
	 * Gets the value of the maxRepeatCount property.
	 * 
	 * @return possible object is {@link Integer }
	 */
	public Integer getMaxRepeatCount() {
		if (maxRepeatCount == null || !(maxRepeatCount > 1))
			return 1;
		return maxRepeatCount;
	}

	/**
	 * Gets the value of the rewardRepeatCount property.
	 * 
	 * @return possible object is {@link Integer }
	 */
	public Integer getRewardRepeatCount() {
		return rewardRepeatCount != null && rewardRepeatCount > 0 ? rewardRepeatCount : 0;
	}

	/**
	 * Gets the value of the cannotShare property.
	 * 
	 * @return possible object is {@link Boolean }
	 */
	public boolean isCannotShare() {
		if (cannotShare == null) {
			return false;
		} else {
			return cannotShare;
		}
	}

	/**
	 * Gets the value of the cannotGiveup property.
	 * 
	 * @return possible object is {@link Boolean }
	 */
	public boolean isCannotGiveup() {
		if (cannotGiveup == null) {
			return false;
		} else {
			return cannotGiveup;
		}
	}

	/**
	 * Gets the value of the canReport property.
	 *
	 * @return possible object is {@link Boolean }
	 */
	public boolean isCanReport() {
		if (canReport == null) {
			return false;
		} else {
			return canReport;
		}
	}

	public boolean isUseSingleClassReward() {
		if (useClassReward == null) {
			return false;
		} else {
			return useClassReward == 1;
		}
	}

	public boolean isUseRepeatedClassReward() {
		if (useClassReward == null) {
			return false;
		} else {
			return useClassReward == 2;
		}
	}

	public boolean isRepeatable() {
		return getMaxRepeatCount() > 1;
	}

	/**
	 * Gets the value of the racePermitted property.
	 * 
	 * @return possible object is {@link Race }
	 */
	public Race getRacePermitted() {
		return racePermitted;
	}

	/**
	 * Gets the value of the combineskill property.
	 * 
	 * @return possible object is {@link Integer }
	 */
	public Integer getCombineSkill() {
		return combineskill;
	}

	/**
	 * Gets the value of the combineSkillpoint property.
	 * 
	 * @return possible object is {@link Integer }
	 */
	public Integer getCombineSkillPoint() {
		return combineSkillpoint;
	}

	/**
	 * Gets the value of the timer property.
	 * 
	 * @return possible object is {@link Integer }
	 */

	public boolean isTimer() {
		if (timer == null) {
			return false;
		} else {
			return timer;
		}
	}

	public QuestCategory getCategory() {
		if (category == null)
			category = QuestCategory.QUEST;
		return category;
	}

	public QuestExtraCategory getExtraCategory() {
		if (extraCategory == null)
			extraCategory = QuestExtraCategory.NONE;
		return extraCategory;
	}

	/**
	 * @return the mentor
	 */
	public boolean isMentor() {
		return mentorType != QuestMentorType.NONE;
	}

	/**
	 * @return the mentor
	 */
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
		return isTimeBased() && repeatCycle.size() == 1 && repeatCycle.get(0) == QuestRepeatCycle.ALL;
	}

	public boolean isWeekly() {
		return isTimeBased() && !isDaily();
	}

	public boolean isMaster() {
		return getCombineSkillPoint() != null && getCombineSkillPoint() == 499;
	}

	public boolean isExpert() {
		return getCombineSkillPoint() != null && getCombineSkillPoint() == 399;
	}

	public boolean isProfession() {
		return isMaster() || isExpert();
	}

	public boolean isNoCount() {
		return category.equals(QuestCategory.NON_COUNT) || category.equals(QuestCategory.EVENT);
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
