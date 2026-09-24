package com.aionemu.gameserver.model.skill;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.slf4j.LoggerFactory;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.templates.npcskill.NpcSkillTemplate;

/**
 * @author ATracer, Yeats, Neon
 */
public class NpcSkillList {

	private final List<NpcSkillEntry> skillsSortedByPrioDescending;

	public NpcSkillList(List<NpcSkillTemplate> npcSkills) {
		this.skillsSortedByPrioDescending = initSkillList(npcSkills);
	}

	private List<NpcSkillEntry> initSkillList(List<NpcSkillTemplate> npcSkills) {
		List<NpcSkillEntry> skillsSortedByPrioDescending = new ArrayList<>(npcSkills.size());
		for (NpcSkillTemplate template : npcSkills) {
			if (DataManager.SKILL_DATA.getSkillTemplate(template.getSkillId()) == null) {
				LoggerFactory.getLogger(NpcSkillList.class).warn("Missing skill data for skill " + template.getSkillId() + " in npc skill list");
				continue;
			}
			skillsSortedByPrioDescending.add(new NpcSkillTemplateEntry(template));
		}
		skillsSortedByPrioDescending.sort(Comparator.comparingInt(NpcSkillEntry::getPriority).reversed());
		return skillsSortedByPrioDescending;
	}

	public int size() {
		return skillsSortedByPrioDescending.size();
	}

	public boolean isEmpty() {
		return skillsSortedByPrioDescending.isEmpty();
	}

	public NpcSkillEntry getRandomSkill() {
		return Rnd.get(skillsSortedByPrioDescending);
	}

	public NpcSkillEntry getSkillOnPosition(int position) {
		if (skillsSortedByPrioDescending.isEmpty())
			return null;
		if (position >= skillsSortedByPrioDescending.size())
			position = skillsSortedByPrioDescending.size() - 1;

		return skillsSortedByPrioDescending.get(position);
	}

	public List<NpcSkillEntry> getPostSpawnSkills() {
		List<NpcSkillEntry> filteredSkills = new ArrayList<>();
		for (NpcSkillEntry skill : skillsSortedByPrioDescending)
			if (skill.hasPostSpawnCondition())
				filteredSkills.add(skill);
		return filteredSkills;
	}

	public List<NpcSkillEntry> getSkillsSortedByPriority(int priority) {
		if (skillsSortedByPrioDescending.isEmpty())
			return Collections.emptyList();

		List<NpcSkillEntry> skillsByPriority = new ArrayList<>();
		for (NpcSkillEntry skill : skillsSortedByPrioDescending) {
			if (skill.getPriority() == priority)
				skillsByPriority.add(skill);
			else if (skill.getPriority() < priority)
				break;
		}
		return skillsByPriority;
	}

	public int[] getPriorities() {
		return skillsSortedByPrioDescending.stream().mapToInt(NpcSkillEntry::getPriority).distinct().toArray();
	}

	public List<NpcSkillEntry> getChainSkillsSortedByPriority(NpcSkillEntry curSkill) {
		if (skillsSortedByPrioDescending.isEmpty())
			return Collections.emptyList();

		List<NpcSkillEntry> chainSkills = new ArrayList<>();
		int id = curSkill.getNextChainId();
		if (id > 0) {
			for (NpcSkillEntry skill : skillsSortedByPrioDescending) {
				if (skill.getChainId() == id) {
					chainSkills.add(skill);
				}
			}
		}
		return chainSkills;
	}
}
