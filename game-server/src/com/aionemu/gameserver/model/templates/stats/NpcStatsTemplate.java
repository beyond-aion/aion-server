package com.aionemu.gameserver.model.templates.stats;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Luno
 * @modified Estrayl, Neon
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "npc_stats_template")
public class NpcStatsTemplate extends StatsTemplate {

	@XmlAttribute(name = "maxXp")
	private int maxXp;

	public int getMaxXp() {
		return maxXp;
	}
	
	public void setMaxXp(int maxXp) {
		this.maxXp = maxXp;
	}
}
