package com.aionemu.gameserver.model.templates.abyss;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;

import com.aionemu.gameserver.model.Race;

/**
 * AP gain and AP loss modifiers of one world for each race.
 */
@XmlAccessorType(XmlAccessType.NONE)
public class AbyssPointWorldMod {

	@XmlAttribute(name = "map_id")
	private int mapId;

	@XmlAttribute(name = "drop_point_light")
	private int dropPointLight = 100;

	@XmlAttribute(name = "drop_point_dark")
	private int dropPointDark = 100;

	@XmlAttribute(name = "death_penalty_light")
	private int deathPenaltyLight = 100;

	@XmlAttribute(name = "death_penalty_dark")
	private int deathPenaltyDark = 100;

	public int getMapId() {
		return mapId;
	}

	public float getDropPointMod(Race race) {
		return (race == Race.ASMODIANS ? dropPointDark : dropPointLight) / 100f;
	}

	public float getDeathPenaltyMod(Race race) {
		return (race == Race.ASMODIANS ? deathPenaltyDark : deathPenaltyLight) / 100f;
	}
}
