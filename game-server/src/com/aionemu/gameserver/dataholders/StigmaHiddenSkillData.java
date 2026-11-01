package com.aionemu.gameserver.dataholders;

import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jakarta.xml.bind.Unmarshaller;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlTransient;

import com.aionemu.gameserver.model.PlayerClass;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.templates.stigma.StigmaHiddenSkill;
import com.aionemu.gameserver.model.templates.stigma.StigmaHiddenSkillClass;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "stigma_hidden_skills")
public class StigmaHiddenSkillData {

	@XmlElement(name = "class")
	private List<StigmaHiddenSkillClass> classes;

	@XmlTransient
	private final Map<PlayerClass, List<StigmaHiddenSkill>> hiddenSkills = new EnumMap<>(PlayerClass.class);

	void afterUnmarshal(Unmarshaller u, Object parent) {
		if (classes == null)
			return;
		for (StigmaHiddenSkillClass playerClass : classes) {
			// stigma combinations win over the class default
			List<StigmaHiddenSkill> skills = playerClass.getHiddenSkills().stream()
				.sorted(Comparator.comparing(StigmaHiddenSkill::hasStigmaConditions).reversed()).toList();
			hiddenSkills.put(playerClass.getPlayerClass(), skills);
		}
		classes = null;
	}

	/**
	 * @return the hidden skill id for the equipped stigma skill groups, or 0
	 */
	public int findHiddenSkill(PlayerClass playerClass, Race race, Set<String> equippedSkillGroups) {
		for (StigmaHiddenSkill skill : hiddenSkills.getOrDefault(playerClass, Collections.emptyList()))
			if (skill.matches(race, equippedSkillGroups))
				return skill.getId();
		return 0;
	}

	public int size() {
		return hiddenSkills.values().stream().mapToInt(List::size).sum();
	}
}
