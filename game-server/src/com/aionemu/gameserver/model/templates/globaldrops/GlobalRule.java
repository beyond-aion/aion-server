package com.aionemu.gameserver.model.templates.globaldrops;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;

/**
 * @author AionCool
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "GlobalRule")
public class GlobalRule {

	@XmlElementWrapper(name = "gd_items", required = true)
	@XmlElement(name = "gd_item", required = true)
	private List<GlobalDropItem> gdItems;

	@XmlElement(name = "gd_maps")
	private GlobalDropMaps gdMaps;

	@XmlElement(name = "gd_races")
	private GlobalDropRaces gdRaces;

	@XmlElement(name = "gd_tribes")
	private GlobalDropTribes gdTribes;

	@XmlElement(name = "gd_ratings")
	private GlobalDropRatings gdRatings;

	@XmlElement(name = "gd_worlds")
	private GlobalDropWorlds gdWorlds;

	@XmlElement(name = "gd_npcs")
	private GlobalDropNpcs gdNpcs;

	@XmlElement(name = "gd_npc_names")
	private GlobalDropNpcNames gdNpcNames;

	@XmlElement(name = "gd_npc_groups")
	private GlobalDropNpcGroups gdNpcGroups;

	@XmlElement(name = "gd_excluded_npcs")
	private GlobalDropExcludedNpcs gdExcludedNpcs;

	@XmlElement(name = "gd_zones")
	private GlobalDropZones gdZones;

	@XmlAttribute(name = "rule_name", required = true)
	private String ruleName;
	@XmlAttribute(name = "min_count")
	private Long minCount = 1L;
	@XmlAttribute(name = "max_count")
	private Long maxCount = 1L;
	@XmlAttribute(name = "base_chance", required = true)
	private float chance;
	@XmlAttribute(name = "min_diff")
	private int minDiff = -99;
	@XmlAttribute(name = "max_diff")
	private int maxDiff = 99;
	@XmlAttribute(name = "restriction_race")
	private RestrictionRace restrictionRace;
	@XmlAttribute(name = "no_reduction")
	private boolean noReduction;
	@XmlAttribute(name = "member_limit")
	private int memberLimit = 1;
	@XmlAttribute(name = "max_drop_rule")
	private int maxDropRule = 1;
	@XmlAttribute(name = "dynamic_chance")
	private boolean dynamicChance;

	public List<GlobalDropItem> getDropItems() {
		return gdItems;
	}

	public GlobalDropWorlds getGlobalRuleWorlds() {
		return gdWorlds;
	}

	public GlobalDropRaces getGlobalRuleRaces() {
		return gdRaces;
	}

	public GlobalDropRatings getGlobalRuleRatings() {
		return gdRatings;
	}

	public GlobalDropMaps getGlobalRuleMaps() {
		return gdMaps;
	}

	public GlobalDropTribes getGlobalRuleTribes() {
		return gdTribes;
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

	public GlobalDropNpcGroups getGlobalRuleNpcGroups() {
		return gdNpcGroups;
	}

	public GlobalDropExcludedNpcs getGlobalRuleExcludedNpcs() {
		return gdExcludedNpcs;
	}

	public GlobalDropZones getGlobalRuleZones() {
		return gdZones;
	}

	public String getRuleName() {
		return ruleName;
	}

	public long getMinCount() {
		return minCount;
	}

	public long getMaxCount() {
		return maxCount;
	}

	public float getChance() {
		return chance;
	}

	public int getMinDiff() {
		return minDiff;
	}

	public int getMaxDiff() {
		return maxDiff;
	}

	public RestrictionRace getRestrictionRace() {
		return restrictionRace;
	}

	public boolean getNoReduction() {
		return noReduction;
	}

	public int getMemberLimit() {
		return memberLimit;
	}

	public int getMaxDropRule() {
		return maxDropRule;
	}

	public boolean isDynamicChance() {
		return dynamicChance;
	}

	@XmlType(name = "race_restriction")
	@XmlEnum
	public enum RestrictionRace {
		ASMODIANS,
		ELYOS;
	}
}
