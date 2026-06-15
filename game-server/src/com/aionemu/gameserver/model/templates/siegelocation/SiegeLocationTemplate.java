package com.aionemu.gameserver.model.templates.siegelocation;

import java.util.Collections;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlList;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.model.siege.SiegeType;
import com.aionemu.gameserver.model.templates.L10n;

/**
 * @author Sarynth, antness, Source, Wakizashi
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "siegelocation")
public class SiegeLocationTemplate implements L10n {

	@XmlAttribute(name = "id")
	protected int id;
	@XmlAttribute(name = "type")
	protected SiegeType type;
	@XmlAttribute(name = "world")
	protected int world;
	@XmlElement(name = "artifact_activation")
	protected ArtifactActivation artifactActivation;
	@XmlElement(name = "door_repair_data")
	protected DoorRepairData doorRepairData;
	@XmlElement(name = "siege_reward")
	protected List<SiegeReward> siegeRewards;
	@XmlElement(name = "legion_reward")
	protected List<SiegeLegionReward> siegeLegionRewards;
	@XmlElement(name = "merc_zone")
	protected List<SiegeMercenaryZone> siegeMercenaryZones;
	@XmlElement(name = "assault_data")
	protected AssaultData assaultData;
	@XmlElement(name = "siege_related_bases")
	protected SiegeRelatedBases siegeRelatedBases;

	@XmlAttribute(name = "name_id")
	protected int nameId = 0;
	@XmlAttribute(name = "siege_duration")
	protected int siegeDuration;
	@XmlAttribute(name = "influence")
	protected int influenceValue;
	@XmlAttribute(name = "occupy_count")
	protected int maxOccupyCount;
	@XmlAttribute(name = "legion_gp")
	protected int legionGp;
	@XmlAttribute(name = "kinah_rewards")
	protected List<Integer> kinahRewards;
	@XmlList
	@XmlAttribute(name = "fortress_dependency")
	protected List<Integer> fortressDependency;

	public int getId() {
		return id;
	}

	public SiegeType getType() {
		return type;
	}

	public int getWorldId() {
		return world;
	}

	public ArtifactActivation getActivation() {
		return artifactActivation;
	}

	public DoorRepairData getDoorRepairData() {
		return doorRepairData;
	}

	public List<SiegeReward> getSiegeRewards() {
		return siegeRewards;
	}

	public List<SiegeLegionReward> getSiegeLegionRewards() {
		return siegeLegionRewards;
	}

	public List<SiegeMercenaryZone> getSiegeMercenaryZones() {
		return siegeMercenaryZones;
	}

	public AssaultData getAssaultData() {
		return assaultData;
	}

	public SiegeRelatedBases getSiegeRelatedBases() {
		return siegeRelatedBases;
	}

	@Override
	public int getL10nId() {
		return nameId;
	}

	public int getRepeatCount() {
		return artifactActivation.getRepeatCount();
	}

	public int getRepeatInterval() {
		return artifactActivation.getRepeatInterval();
	}

	public List<Integer> getFortressDependency() {
		if (fortressDependency == null)
			return Collections.emptyList();
		return fortressDependency;
	}

	/**
	 * @return the Duration in Seconds
	 */
	public int getSiegeDuration() {
		return siegeDuration;
	}

	public int getInfluenceValue() {
		return influenceValue;
	}

	public int getMaxOccupyCount() {
		return maxOccupyCount;
	}

	public int getLegionGp() {
		return legionGp;
	}

	public int getKinahRewardByRewardLevel(int rewardLevel) {
		if (kinahRewards == null || rewardLevel > kinahRewards.size() - 1)
			return 0;
		return kinahRewards.get(rewardLevel);
	}
}
