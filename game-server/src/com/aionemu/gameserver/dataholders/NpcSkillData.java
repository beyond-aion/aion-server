package com.aionemu.gameserver.dataholders;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.*;

import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.model.templates.npcskill.NpcSkillTemplates;

/**
 * @author ATracer
 */
@XmlRootElement(name = "npc_skill_templates")
@XmlAccessorType(XmlAccessType.FIELD)
public class NpcSkillData {

	@XmlElement(name = "npc_skills")
	private List<NpcSkillTemplates> npcSkills;

	@XmlTransient
	private final Map<Integer, NpcSkillTemplates> npcSkillData = new HashMap<>();

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

	public NpcSkillTemplates getNpcSkillList(int id) {
		return npcSkillData.get(id);
	}

	public void setNpcSkillTemplates(List<NpcSkillTemplates> template) {
		this.npcSkills = template;
		npcSkillData.clear();
		afterUnmarshal(null, null);
	}

	public Collection<NpcSkillTemplates> getAllNpcSkillTemplates() {
		return npcSkillData.values();
	}
}
