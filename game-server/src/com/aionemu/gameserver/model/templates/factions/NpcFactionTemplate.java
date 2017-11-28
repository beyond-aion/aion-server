package com.aionemu.gameserver.model.templates.factions;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.templates.L10n;

/**
 * @author vlog
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "NpcFaction")
public class NpcFactionTemplate implements L10n {

	@XmlAttribute(name = "id", required = true)
	private int id;
	@XmlAttribute(name = "name")
	private String name;
	@XmlAttribute(name = "name_id")
	private int nameId;
	@XmlAttribute(name = "category")
	private FactionCategory category;
	@XmlAttribute(name = "min_level")
	private Integer minLevel;
	@XmlAttribute(name = "max_level")
	private int maxLevel = 99;
	@XmlAttribute(name = "race")
	private Race race;
	@XmlAttribute(name = "npc_ids")
	private List<Integer> npcIds;
	@XmlAttribute(name = "skill_points")
	private int skillPoints;

	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	@Override
	public int getL10nId() {
		return nameId;
	}

	public FactionCategory getCategory() {
		return category;
	}

	public int getMinLevel() {
		return minLevel;
	}

	public int getMaxLevel() {
		return maxLevel;
	}

	public Race getRace() {
		return race;
	}

	public boolean isMentor() {
		return category == FactionCategory.MENTOR;
	}

	public List<Integer> getNpcIds() {
		return npcIds;
	}

	public int getSkillPoints() {
		return skillPoints;
	}
}
