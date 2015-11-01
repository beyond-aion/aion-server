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

/**
 * @author Sarynth modified by antness & Source & Wakizashi
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "siegelocation")
public class SiegeLocationTemplate {

	@XmlAttribute(name = "id")
	protected int id;
	@XmlAttribute(name = "type")
	protected SiegeType type;
	@XmlAttribute(name = "world")
	protected int world;
	@XmlElement(name = "artifact_activation")
	protected ArtifactActivation artifactActivation;
	@XmlElement(name = "siege_reward")
	protected List<SiegeReward> siegeRewards;
	@XmlElement(name = "legion_reward")
	protected List<SiegeLegionReward> siegeLegionRewards;
	@XmlElement(name = "merc_zone")
	protected List<SiegeMercenaryZone> siegeMercenaryZones;
	@XmlAttribute(name = "name_id")
	protected int nameId = 0;
	@XmlAttribute(name = "repeat_count")
	protected int repeatCount = 1;
	@XmlAttribute(name = "repeat_interval")
	protected int repeatInterval = 1;
	@XmlAttribute(name = "siege_duration")
	protected int siegeDuration;
	@XmlAttribute(name = "influence")
	protected int influenceValue;
	@XmlAttribute(name = "occupy_count")
	protected int maxOccupyCount;
	@XmlList
	@XmlAttribute(name = "fortress_dependency")
	protected List<Integer> fortressDependency;

	/**
	 * @return the location id
	 */
	public int getId() {
		return this.id;
	}

	/**
	 * @return the location type
	 */
	public SiegeType getType() {
		return this.type;
	}

	/**
	 * @return the world id
	 */
	public int getWorldId() {
		return this.world;
	}

	public ArtifactActivation getActivation() {
		return this.artifactActivation;
	}

	/**
	 * @return the reward list
	 */
	public List<SiegeReward> getSiegeRewards() {
		return this.siegeRewards;
	}

	/**
	 * @return the legion rewards
	 */
	public List<SiegeLegionReward> getSiegeLegionRewards() {
		return this.siegeLegionRewards;
	}
	
	/**
	 * @return the mercenary zones
	 */
	public List<SiegeMercenaryZone> getSiegeMercenaryZones() {
		return this.siegeMercenaryZones;
	}

	/**
	 * @return the nameId
	 */
	public int getNameId() {
		return nameId;
	}

	/**
	 * @return the repeatCount
	 */
	public int getRepeatCount() {
		return repeatCount;
	}

	/**
	 * @return the repeatInterval
	 */
	public int getRepeatInterval() {
		return repeatInterval;
	}

	/**
	 * @return the fortressDependency
	 */
	public List<Integer> getFortressDependency() {
		if (fortressDependency == null)
			return Collections.emptyList();
		return fortressDependency;
	}

	/**
	 * @return the Duration in Seconds
	 */
	public int getSiegeDuration() {
		return this.siegeDuration;
	}

	/**
	 * @return the influence Points
	 */
	public int getInfluenceValue() {
		return this.influenceValue;
	}

	/**
	 * @return occupyCount
	 */
	public int getMaxOccupyCount() {
		return this.maxOccupyCount;
	}
}
