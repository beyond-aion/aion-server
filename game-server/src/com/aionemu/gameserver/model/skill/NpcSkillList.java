package com.aionemu.gameserver.model.skill;

import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.templates.npcskill.NpcSkillTemplate;
import com.aionemu.gameserver.model.templates.npcskill.NpcSkillTemplates;

/**
 * @author ATracer, Yeats, Neon
 */
public class NpcSkillList {

	private static final Logger log = LoggerFactory.getLogger(NpcSkillList.class);

	private List<NpcSkillEntry> skills;
	private int[] priorities;

	public NpcSkillList(Npc owner) {
		initSkillList(owner.getNpcId());
	}

	private void initSkillList(int npcId) {
		NpcSkillTemplates npcSkillTemplates = DataManager.NPC_SKILL_DATA.getNpcSkillList(npcId);
		List<NpcSkillTemplate> npcSkills = npcSkillTemplates == null ? null : npcSkillTemplates.getNpcSkills();
		if (npcSkills == null || npcSkills.isEmpty()) {
			skills = Collections.emptyList();
		} else {
			skills = new ArrayList<>(npcSkills.size());
			List<Integer> prios = new ArrayList<>();
			for (Iterator<NpcSkillTemplate> iter = npcSkills.iterator(); iter.hasNext();) {
				NpcSkillTemplate template = iter.next();
				if (DataManager.SKILL_DATA.getSkillTemplate(template.getSkillId()) == null) {
					log.warn("Missing skill " + template.getSkillId() + " for npc " + npcId);
					iter.remove();
					continue;
				}
				skills.add(new NpcSkillTemplateEntry(template));
				if (!prios.contains(template.getPriority())) {
					prios.add(template.getPriority());
				}
			}
			prios.sort(Comparator.reverseOrder());
			priorities = prios.stream().mapToInt(Integer::intValue).toArray();
		}
	}

	public boolean isEmpty() {
		return skills.isEmpty();
	}

	public NpcSkillEntry getRandomSkill() {
		return Rnd.get(skills);
	}

	public NpcSkillEntry getSkillOnPosition(int position) {
		if (skills.isEmpty())
			return null;
		if (position >= skills.size())
			position = skills.size() - 1;

		return skills.get(position);
	}

	public List<NpcSkillEntry> getPostSpawnSkills() {
		List<NpcSkillEntry> filteredSkills = new ArrayList<>();
		for (NpcSkillEntry skill : skills)
			if (skill.hasPostSpawnCondition())
				filteredSkills.add(skill);
		return filteredSkills;
	}

	public List<NpcSkillEntry> getNpcSkills() {
		return skills;
	}

	public List<NpcSkillEntry> getSkillsByPriority(int priority) {
		if (skills.isEmpty())
			return Collections.emptyList();

		List<NpcSkillEntry> skillsByPriority = new ArrayList<>();
		for (NpcSkillEntry skill : skills) {
			if (skill.getPriority() == priority) {
				skillsByPriority.add(skill);
			}
		}
		return skillsByPriority;

	}

	public int[] getPriorities() {
		return priorities;
	}

	public List<NpcSkillEntry> getChainSkills(NpcSkillEntry curSkill) {
		if (skills.isEmpty())
			return Collections.emptyList();

		List<NpcSkillEntry> chainSkills = new ArrayList<>();
		int id = curSkill.getNextChainId();
		if (id > 0) {
			for (NpcSkillEntry skill : skills) {
				if (skill.getChainId() == id) {
					chainSkills.add(skill);
				}
			}
		}
		return chainSkills;
	}
}
