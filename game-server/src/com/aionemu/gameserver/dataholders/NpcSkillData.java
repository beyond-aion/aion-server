package com.aionemu.gameserver.dataholders;

import gnu.trove.map.hash.TIntObjectHashMap;

import java.util.List;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

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

	/** A map containing all npc skill templates */
	private TIntObjectHashMap<NpcSkillTemplates> npcSkillData = new TIntObjectHashMap<>();

	void afterUnmarshal(Unmarshaller u, Object parent) {
		for (NpcSkillTemplates npcSkill : npcSkills) {
			for (Integer npcId : npcSkill.getNpcIds())
				npcSkillData.put(npcId, npcSkill);

			if (npcSkill.getNpcSkills() == null)
				LoggerFactory.getLogger(NpcSkillData.class).error("NO SKILL");
		}

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
	
	public List<NpcSkillTemplates> getAllNpcSkillTemplates() {
		return npcSkills;
	}
}
