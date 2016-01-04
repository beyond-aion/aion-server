package com.aionemu.gameserver.dataholders;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.templates.serial_killer.RankRestriction;

/**
 * @author Dtem
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "rankRestriction" })
@XmlRootElement(name = "serial_killers")
public class SerialKillerData {

	@XmlElement(name = "rank_restriction")
	protected List<RankRestriction> rankRestriction;

	public RankRestriction getRankRestriction(String type, Race race, int rank) {
		for (RankRestriction template : rankRestriction) {
			if (template.getType().equals(type) && template.getRace() == race && template.getRankNum() == rank)
				return template;
		}
		return null;
	}

	public int size() {
		return rankRestriction.size();
	}
}
