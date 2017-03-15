package com.aionemu.gameserver.model.templates.serial_killer;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.model.Race;

/**
 * @author Dtem
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "RankRestriction", propOrder = { "penaltyAttr" })
public class RankRestriction {

	@XmlElement(name = "penalty_attr")
	protected List<RankPenaltyAttr> penaltyAttr;
	@XmlAttribute(name = "type", required = true)
	protected String type;
	@XmlAttribute(name = "race", required = true)
	private Race race;
	@XmlAttribute(name = "rank_num", required = true)
	protected int rankNum;
	@XmlAttribute(name = "restrict_direct_portal", required = true)
	protected boolean restrictDirectPortal;
	@XmlAttribute(name = "restrict_dynamic_bindstone", required = true)
	protected boolean restrictDynamicBindstone;

	/**
	 * @return the restrictDirectPortal
	 */
	public boolean isRestrictDirectPortal() {
		return restrictDirectPortal;
	}

	/**
	 * @param restrictDirectPortal
	 *          the restrictDirectPortal to set
	 */
	public void setRestrictDirectPortal(boolean restrictDirectPortal) {
		this.restrictDirectPortal = restrictDirectPortal;
	}

	/**
	 * @return the restrictDynamicBindstone
	 */
	public boolean isRestrictDynamicBindstone() {
		return restrictDynamicBindstone;
	}

	/**
	 * @param restrictDynamicBindstone
	 *          the restrictDynamicBindstone to set
	 */
	public void setRestrictDynamicBindstone(boolean restrictDynamicBindstone) {
		this.restrictDynamicBindstone = restrictDynamicBindstone;
	}

	public List<RankPenaltyAttr> getPenaltyAttr() {
		if (penaltyAttr == null)
			penaltyAttr = new ArrayList<>();
		return this.penaltyAttr;
	}

	public int getRankNum() {
		return rankNum;
	}

	public Race getRace() {
		return race;
	}

	public String getType() {
		return type;
	}

	public void setRankNum(int value) {
		this.rankNum = value;
	}
}
