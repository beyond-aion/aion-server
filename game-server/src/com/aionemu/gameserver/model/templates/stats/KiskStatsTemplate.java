package com.aionemu.gameserver.model.templates.stats;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlRootElement;

/**
 * @author Sarynth
 */

@XmlRootElement(name = "kisk_stats")
@XmlAccessorType(XmlAccessType.FIELD)
public class KiskStatsTemplate {

	@XmlAttribute(name = "usemask")
	private int useMask = 4;

	@XmlAttribute(name = "members")
	private int maxMembers = 6;

	@XmlAttribute(name = "resurrects")
	private int maxResurrects = 18;

	public int getUseMask() {
		return useMask;
	}

	public int getMaxMembers() {
		return maxMembers;
	}

	public int getMaxResurrects() {
		return maxResurrects;
	}
}
