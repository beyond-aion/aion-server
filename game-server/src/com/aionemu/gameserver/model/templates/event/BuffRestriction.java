package com.aionemu.gameserver.model.templates.event;

import java.util.Set;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlList;

import com.aionemu.gameserver.model.templates.event.Buff.BuffMapType;

/**
 * @author Neon
 */
public class BuffRestriction {

	@XmlList
	@XmlAttribute(name = "maps")
	private Set<BuffMapType> maps;
	@XmlAttribute(name = "team_size_max_percent")
	private float teamSizeMaxPercent;
	@XmlAttribute(name = "random_days_per_month")
	private int randomDaysPerMonth;

	public Set<BuffMapType> getMaps() {
		return maps;
	}

	public float getTeamSizeMaxPercent() {
		return teamSizeMaxPercent;
	}

	public int getRandomDaysPerMonth() {
		return randomDaysPerMonth;
	}
}
