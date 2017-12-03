package com.aionemu.gameserver.model.templates.globaldrops;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;

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
	protected int memberLimit = 1;
	@XmlAttribute(name = "max_drop_rule")
	protected int maxDropRule = 1;
	@XmlAttribute(name = "fixed_chance")
	protected boolean fixedChance;

	public GlobalDropItems getGlobalRuleItems() {
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
		if (minCount == null) {
			return 1L;
		} else {
			return minCount;
		}
	}

	public long getMaxCount() {
		if (maxCount == null) {
			return 1L;
		} else {
			return maxCount;
		}
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
