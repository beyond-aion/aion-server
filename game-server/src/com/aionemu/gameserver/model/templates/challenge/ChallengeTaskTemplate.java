package com.aionemu.gameserver.model.templates.challenge;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.templates.L10n;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ChallengeTask", propOrder = { "quest", "contrib", "reward" })
public class ChallengeTaskTemplate implements L10n {

	@XmlElement(required = true)
	private List<ChallengeQuestTemplate> quest;
	private List<ContributionReward> contrib;

	@XmlElement(required = true)
	private ChallengeReward reward;

	@XmlAttribute
	private boolean repeat;

	@XmlAttribute(name = "town_residence")
	private boolean townResidence;

	@XmlAttribute(name = "name_id")
	private int nameId;

	@XmlAttribute(name = "max_level", required = true)
	private int maxLevel;

	@XmlAttribute(name = "min_level", required = true)
	private int minLevel;

	@XmlAttribute(name = "prev_task")
	private Integer prevTask;

	@XmlAttribute(required = true)
	private Race race;

	@XmlAttribute(required = true)
	private ChallengeType type;

	@XmlAttribute(required = true)
	private int id;

	@XmlAttribute(name = "legion_level_task")
	private boolean legionLevelTask = false;

	public List<ChallengeQuestTemplate> getQuests() {
		return quest;
	}

	public List<ContributionReward> getContrib() {
		return contrib;
	}

	public ChallengeReward getReward() {
		return reward;
	}

	public boolean isRepeatable() {
		return repeat;
	}

	public boolean isTownResidence() {
		return townResidence;
	}

	@Override
	public int getL10nId() {
		return nameId;
	}

	public int getMaxLevel() {
		return maxLevel;
	}

	public int getMinLevel() {
		return minLevel;
	}

	public Integer getPrevTask() {
		return prevTask;
	}

	public Race getRace() {
		return race;
	}

	public ChallengeType getType() {
		return type;
	}

	public int getId() {
		return id;
	}

	public boolean isLegionLevelTask() {
		return legionLevelTask;
	}
}
