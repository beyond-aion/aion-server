package com.aionemu.gameserver.model.templates;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.model.Race;

/**
 * @author Yeats
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Legion_dominion_location")
public class LegionDominionLocationTemplate {

	@XmlAttribute(name = "id")
	protected int id;
	@XmlAttribute(name = "worldId")
	protected int worldId;
	@XmlAttribute(name = "race")
	protected Race race;
	@XmlAttribute(name = "zone")
	protected String zone;
	@XmlAttribute(name = "nameId")
	protected int nameId;
	@XmlElement(name = "reward")
	protected List<LegionDominionReward> reward;
	
	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}
	
	/**
	 * @return the worldId
	 */
	public int getWorldId() {
		return worldId;
	}
	
	/**
	 * @return the race
	 */
	public Race getRace() {
		return race;
	}
	
	/**
	 * @return the zone
	 */
	public String getZone() {
		return zone;
	}

	
	/**
	 * @return the reward
	 */
	public List<LegionDominionReward> getRewards() {
		return reward;
	}

	public int getNameId() {
		return nameId;
	}
}
