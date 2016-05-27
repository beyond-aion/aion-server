package com.aionemu.gameserver.model.templates.globaldrops;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;

//import com.aionemu.gameserver.model.templates.globaldrops.GlobalRule;

/**
 * @author AionCool
 */

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "GlobalRule")
public class GlobalRule {

	@XmlElement(name = "gd_items", required = false)
	protected GlobalDropItems gdItems;

	@XmlElement(name = "gd_maps", required = false)
	protected GlobalDropMaps gdMaps;

	@XmlElement(name = "gd_races", required = false)
	protected GlobalDropRaces gdRaces;

	@XmlElement(name = "gd_tribes", required = false)
	protected GlobalDropTribes gdTribes;

	@XmlElement(name = "gd_ratings", required = false)
	protected GlobalDropRatings gdRatings;

	@XmlElement(name = "gd_worlds", required = false)
	protected GlobalDropWorlds gdWorlds;

	@XmlElement(name = "gd_npcs", required = false)
	protected GlobalDropNpcs gdNpcs;

	@XmlElement(name = "gd_npc_names", required = false)
	protected GlobalDropNpcNames gdNpcNames;

	@XmlElement(name = "gd_npc_groups", required = false)
	protected GlobalDropNpcGroups gdNpcGroups;

	@XmlElement(name = "gd_excluded_npcs", required = false)
	protected GlobalDropExcludedNpcs gdExcludedNpcs;

	@XmlElement(name = "gd_zones", required = false)
	protected GlobalDropZones gdZones;

	@XmlAttribute(name = "rule_name", required = true)
	protected String ruleName;
	@XmlAttribute(name = "min_count")
	protected Long minCount = 1L;
	@XmlAttribute(name = "max_count")
	protected Long maxCount = 1L;
	@XmlAttribute(name = "base_chance", required = true)
	protected float chance;
	@XmlAttribute(name = "min_diff")
	protected int minDiff = -99;
	@XmlAttribute(name = "max_diff")
	protected int maxDiff = 99;
	@XmlAttribute(name = "restriction_race")
	protected RestrictionRace restrictionRace;
	@XmlAttribute(name = "no_reduction")
	protected boolean noReduction;
	@XmlAttribute(name = "member_limit")
	protected int memberLimit;
	@XmlAttribute(name = "max_drop_rule")
	protected int maxDropRule = 1;
	@XmlAttribute(name = "fixed_chance")
	protected boolean fixedChance;

	public GlobalDropItems getGlobalRuleItems() {
		return gdItems;
	}

	public void setItems(GlobalDropItems value) {
		this.gdItems = value;
	}

	public GlobalDropWorlds getGlobalRuleWorlds() {
		return gdWorlds;
	}

	public void setWorlds(GlobalDropWorlds value) {
		this.gdWorlds = value;
	}

	public GlobalDropRaces getGlobalRuleRaces() {
		return gdRaces;
	}

	public void setNpcRaces(GlobalDropRaces value) {
		this.gdRaces = value;
	}

	public GlobalDropRatings getGlobalRuleRatings() {
		return gdRatings;
	}

	public void setNpcRatings(GlobalDropRatings value) {
		this.gdRatings = value;
	}

	public GlobalDropMaps getGlobalRuleMaps() {
		return gdMaps;
	}

	public void setMaps(GlobalDropMaps value) {
		this.gdMaps = value;
	}

	public GlobalDropTribes getGlobalRuleTribes() {
		return gdTribes;
	}

	public void setNpcTribes(GlobalDropTribes value) {
		this.gdTribes = value;
	}

	public GlobalDropNpcs getGlobalRuleNpcs() {
		return gdNpcs;
	}

	public void setNpcs(GlobalDropNpcs value) {
		this.gdNpcs = value;
	}

	public GlobalDropNpcNames getGlobalRuleNpcNames() {
		return gdNpcNames;
	}

	public void setNpcNames(GlobalDropNpcNames value) {
		this.gdNpcNames = value;
	}

	public GlobalDropNpcGroups getGlobalRuleNpcGroups() {
		return gdNpcGroups;
	}

	public void setNpcGroups(GlobalDropNpcGroups value) {
		this.gdNpcGroups = value;
	}

	public GlobalDropExcludedNpcs getGlobalRuleExcludedNpcs() {
		return gdExcludedNpcs;
	}

	public void setExcludedNpcs(GlobalDropExcludedNpcs value) {
		this.gdExcludedNpcs = value;
	}

	public GlobalDropZones getGlobalRuleZones() {
		return gdZones;
	}

	public void setZones(GlobalDropZones value) {
		this.gdZones = value;
	}

	public String getRuleName() {
		return ruleName;
	}

	public void setRuleName(String value) {
		this.ruleName = value;
	}

	public long getMinCount() {
		if (minCount == null) {
			return 1L;
		} else {
			return minCount;
		}
	}

	public void setMinCount(Long value) {
		this.minCount = value;
	}

	public long getMaxCount() {
		if (maxCount == null) {
			return 1L;
		} else {
			return maxCount;
		}
	}

	public void setMaxCount(Long value) {
		this.maxCount = value;
	}

	public float getChance() {
		return chance;
	}

	public void setChance(float value) {
		this.chance = value;
	}

	public int getMinDiff() {
		return minDiff;
	}

	public void setMinDiff(int value) {
		this.minDiff = value;
	}

	public int getMaxDiff() {
		return maxDiff;
	}

	public void setMaxDiff(int value) {
		this.maxDiff = value;
	}

	public RestrictionRace getRestrictionRace() {
		return restrictionRace;
	}

	public void setRestrictionRace(RestrictionRace value) {
		this.restrictionRace = value;
	}

	public boolean getNoReduction() {
		return noReduction;
	}

	public void setNoReduction(boolean value) {
		this.noReduction = value;
	}
	
	public int getMemberLimit() {
		return memberLimit;
	}

	public int getMaxDropRule() {
		return maxDropRule;
	}

	public boolean isFixedChance() {
		return fixedChance;
	}

	@XmlType(name = "race_restriction")
	@XmlEnum
	public enum RestrictionRace {
		ASMODIANS,
		ELYOS;
	}
}
