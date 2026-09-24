package com.aionemu.gameserver.dataholders;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.*;

import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.model.skill.NpcSkillList;
import com.aionemu.gameserver.model.templates.npcskill.NpcSkillTemplate;
import com.aionemu.gameserver.model.templates.npcskill.NpcSkillTemplates;

/**
 * @author ATracer
 */
@XmlRootElement(name = "npc_skill_templates")
@XmlAccessorType(XmlAccessType.FIELD)
public class NpcSkillData {

	private static final NpcSkillList EMPTY = new NpcSkillList(Collections.emptyList());

	@XmlElement(name = "npc_skills")
	private List<NpcSkillTemplates> npcSkills;

	@XmlTransient
	private final Map<Integer, NpcSkillTemplates> npcSkillData = new HashMap<>();

	@XmlTransient
	private final Map<NpcSkillTemplates, NpcSkillList> skillListByTemplate = new ConcurrentHashMap<>();

	void afterUnmarshal(Unmarshaller u, Object parent) {
		for (NpcSkillTemplates npcSkillList : npcSkills) {
			for (Integer npcId : npcSkillList.getNpcIds()) {
				if (npcSkillData.putIfAbsent(npcId, npcSkillList) != null)
					LoggerFactory.getLogger(NpcSkillData.class).warn("Npc " + npcId + " has multiple skill lists in npc_skills.xml");
			}
		}
		npcSkills = null;
	}

	public int size() {
		return npcSkillData.size();
	}

	public List<NpcSkillTemplate> getNpcSkillTemplates(int npcId) {
		NpcSkillTemplates npcSkillTemplates = npcSkillData.get(npcId);
		return npcSkillTemplates == null ? Collections.emptyList() : npcSkillTemplates.getNpcSkills();
	}

	public void setSkillData(List<NpcSkillData> template) {
		this.npcSkills = template.stream().flatMap(t -> t.npcSkillData.values().stream().distinct()).toList();
		npcSkillData.clear();
		skillListByTemplate.clear();
		afterUnmarshal(null, null);
	}

	public NpcSkillList getOrCreateNpcSkillList(int npcId) {
		NpcSkillTemplates npcSkillTemplates = npcSkillData.get(npcId);
		if (npcSkillTemplates == null || npcSkillTemplates.getNpcSkills().isEmpty())
			return EMPTY;
		return skillListByTemplate.computeIfAbsent(npcSkillTemplates, t -> new NpcSkillList(t.getNpcSkills()));
	}
}
