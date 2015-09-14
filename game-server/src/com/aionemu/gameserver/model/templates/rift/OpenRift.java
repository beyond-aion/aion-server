package com.aionemu.gameserver.model.templates.rift;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

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
