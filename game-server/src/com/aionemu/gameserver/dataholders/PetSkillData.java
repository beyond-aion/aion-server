package com.aionemu.gameserver.dataholders;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.*;

import com.aionemu.gameserver.model.templates.petskill.PetSkillTemplate;

/**
 * @author ATracer
 */
@XmlRootElement(name = "pet_skill_templates")
@XmlAccessorType(XmlAccessType.FIELD)
public class PetSkillData {

	@XmlElement(name = "pet_skill")
	private List<PetSkillTemplate> petSkills;

	@XmlTransient
	private final Map<Integer, Map<Integer, Integer>> petSkillData = new HashMap<>();
	@XmlTransient
	private final Map<Integer, List<Integer>> petSkillsMap = new HashMap<>();

	void afterUnmarshal(Unmarshaller u, Object parent) {
		for (PetSkillTemplate petSkill : petSkills) {
			petSkillData.computeIfAbsent(petSkill.getOrderSkill(), k -> new HashMap<>()).put(petSkill.getPetId(), petSkill.getSkillId());
			petSkillsMap.computeIfAbsent(petSkill.getPetId(), k -> new ArrayList<>()).add(petSkill.getSkillId());
		}
		petSkills = null;
	}

	public int size() {
		return petSkillData.size();
	}

	public boolean isPetOrderSkill(int orderSkill) {
		return petSkillData.containsKey(orderSkill);
	}

	public int getPetOrderSkill(int orderSkill, int petNpcId) {
		return petSkillData.get(orderSkill).get(petNpcId);
	}

	public boolean petHasSkill(int petNpcId, int skillId) {
		return petSkillsMap.get(petNpcId).contains(skillId);
	}
}
