package com.aionemu.gameserver.model.templates.stigma;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlList;
import jakarta.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.model.Race;

/**
 * A skill granted by six equipped enhanceable stigmas. Without stigma conditions it is the class default.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "StigmaHiddenSkill")
public class StigmaHiddenSkill {

	@XmlAttribute(required = true)
	private int id;
	@XmlAttribute
	private Race race;
	/**
	 * Skill group of the stigma that must be equipped
	 */
	@XmlAttribute
	private String key;
	/**
	 * Skill groups of which at least two stigmas must be equipped as well
	 */
	@XmlList
	@XmlAttribute(name = "two_of")
	private List<String> twoOf;

	public int getId() {
		return id;
	}

	public boolean hasStigmaConditions() {
		return key != null;
	}

	public boolean matches(Race playerRace, Set<String> equippedSkillGroups) {
		if (race != null && race != playerRace)
			return false;
		if (key == null)
			return true;
		if (!equippedSkillGroups.contains(key))
			return false;
		List<String> options = twoOf == null ? Collections.emptyList() : twoOf;
		return options.stream().filter(equippedSkillGroups::contains).count() >= 2;
	}
}
