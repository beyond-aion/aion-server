package com.aionemu.gameserver.model.skill;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.templates.npcskill.NpcSkillTemplate;
import com.aionemu.gameserver.model.templates.npcskill.NpcSkillTemplates;

/**
 * @author ATracer
 * @modified Yeats
 */
public class NpcSkillList implements SkillList<Npc> {

	private static final Logger log = LoggerFactory.getLogger(NpcSkillList.class);

	private List<NpcSkillEntry> skills;
	private List<Integer> priorities;

	public NpcSkillList(Npc owner) {
		initSkillList(owner.getNpcId());
	}

	private void initSkillList(int npcId) {
		NpcSkillTemplates npcSkillList = DataManager.NPC_SKILL_DATA.getNpcSkillList(npcId);
		if (npcSkillList != null) {
			initSkills();
			for (int index = npcSkillList.getNpcSkills().size() - 1; index >= 0; index--) {
				NpcSkillTemplate template = npcSkillList.getNpcSkills().get(index);
				if (DataManager.SKILL_DATA.getSkillTemplate(template.getSkillId()) == null) {
					log.warn("Missing skill " + template.getSkillId() + " for npc " + npcId);
					npcSkillList.getNpcSkills().remove(index);
					continue;
				}
				skills.add(new NpcSkillTemplateEntry(template));
				if (!priorities.contains(template.getPriority())) {
					priorities.add(template.getPriority());
				}
			}
			priorities.sort(null);
		}
	}

	@Override
	public boolean addSkill(Npc creature, int skillId, int skillLevel) {
		initSkills();
		skills.add(new NpcSkillParameterEntry(skillId, skillLevel));
		return true;
	}

	@Override
	public boolean removeSkill(int skillId) {
		Iterator<NpcSkillEntry> iter = skills.iterator();
		while (iter.hasNext()) {
			NpcSkillEntry next = iter.next();
			if (next.getSkillId() == skillId) {
				iter.remove();
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean isSkillPresent(int skillId) {
		if (skills == null) {
			return false;
		}
		return getSkill(skillId) != null;
	}

	@Override
	public int getSkillLevel(int skillId) {
		return getSkill(skillId).getSkillLevel();
	}

	@Override
	public int size() {
		return skills != null ? skills.size() : 0;
	}

	private void initSkills() {
		if (skills == null) {
			skills = new ArrayList<>();
		}
		
		if (priorities == null) {
			priorities = new ArrayList<>();
		}
	}

	public NpcSkillEntry getRandomSkill() {
		if (skills == null)
			return null;
		return Rnd.get(skills);
	}

	public NpcSkillEntry getSkillOnPosition(int position) {
		if (skills == null || skills.size() == 0)
			return null;
		if (position >= skills.size())
			position = skills.size() - 1;

		return skills.get(position);
	}

	private SkillEntry getSkill(int skillId) {
		for (SkillEntry entry : skills) {
			if (entry.getSkillId() == skillId) {
				return entry;
			}
		}
		return null;
	}

	public NpcSkillEntry getUseInSpawnedSkill() {
		if (this.skills == null)
			return null;
		Iterator<NpcSkillEntry> iter = skills.iterator();
		while (iter.hasNext()) {
			NpcSkillEntry next = iter.next();
			NpcSkillTemplateEntry tmpEntry = (NpcSkillTemplateEntry) next;
			if (tmpEntry.UseInSpawned()) {
				return next;
			}
		}
		return null;
	}

	public List<NpcSkillEntry> getNpcSkills() {
		return skills;
	}
	
	public List<NpcSkillEntry> getSkillsByPriority(int priority) {
		if (skills != null && priorities != null && priorities.contains(priority)) {
			List<NpcSkillEntry> skillsByPriority = new ArrayList<>();
			
			for (NpcSkillEntry entry : skills) {
				if (entry.getPriority() == priority) {
					skillsByPriority.add(entry);
				}
			}
			return skillsByPriority;
			
		} else {
			return null;
		}
	}
	
	public List<Integer> getPriorities() {
		return priorities;
	}
	
	public List<NpcSkillEntry> getChainSkills(NpcSkillEntry curSkill) {
		List<NpcSkillEntry> chainSkills = new ArrayList<>();
		if (skills != null && curSkill != null) {
			int id = curSkill.getNextChainId();
			if (id > 0) {
				for (NpcSkillEntry entry : skills) {
					if (entry != null && entry.getChainId() == id) {
						chainSkills.add(entry);
					}
				}
			}
		}
		return chainSkills;
	}
}
