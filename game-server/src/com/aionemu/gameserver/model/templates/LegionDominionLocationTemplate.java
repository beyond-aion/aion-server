package com.aionemu.gameserver.model.templates;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.model.Race;

/**
 * @author Yeats, Sykra
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "LegionDominionLocation")
public class LegionDominionLocationTemplate implements L10n {

	@XmlAttribute(name = "id")
	protected int id;
	@XmlAttribute(name = "world_id")
	protected int worldId;
	@XmlAttribute(name = "race")
	protected Race race;
	@XmlAttribute(name = "zone")
	protected String zone;
	@XmlAttribute(name = "name_id")
	protected int nameId;
	@XmlElement(name = "reward")
	protected List<LegionDominionReward> reward;
	@XmlElement(name = "invasion_rift")
	protected LegionDominionInvasionRift invasionRift;

	public int getId() {
		return id;
	}

	public int getWorldId() {
		return worldId;
	}

	public Race getRace() {
		return race;
	}

	public String getZone() {
		return zone;
	}

	public List<LegionDominionReward> getRewards() {
		return reward;
	}

	public LegionDominionInvasionRift getInvasionRift() {
		return invasionRift;
	}

	@Override
	public int getL10nId() {
		return nameId;
	}

}
