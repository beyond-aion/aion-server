package com.aionemu.gameserver.model.templates.rift;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlType;

/**
 * @author Source
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "OpenRift")
public class OpenRift {

	@XmlAttribute(name = "schedule")
	protected String schedule;
	@XmlAttribute(name = "spawn")
	protected boolean guards;

	public String getSchedule() {
		return schedule;
	}

	public boolean spawnGuards() {
		return guards;
	}

}
