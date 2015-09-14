package com.aionemu.gameserver.dataholders;

import gnu.trove.map.hash.TIntObjectHashMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.aionemu.gameserver.skillengine.model.SkillTemplate;

/**
 * @author ATracer
 */
@XmlRootElement(name = "skill_data")
@XmlAccessorType(XmlAccessType.FIELD)
public class SkillData {

	@XmlElement(name = "skill_template")
	private List<SkillTemplate> skillTemplates;
	private HashMap<Integer, ArrayList<Integer>> cooldownGroups;
	/**
	 * Map that contains skillId - SkillTemplate key-value pair
	 */
	private TIntObjectHashMap<SkillTemplate> skillData = new TIntObjectHashMap<SkillTemplate>();

	void afterUnmarshal(Unmarshaller u, Object parent) {
		skillData.clear();
		for (SkillTemplate skillTempalte : skillTemplates) {
			skillData.put(skillTempalte.getSkillId(), skillTempalte);
		}
	}

	/**
	 * @param skillId
	 * @return SkillTemplate
	 */
	public SkillTemplate getSkillTemplate(int skillId) {
		return skillData.get(skillId);
	}

	/**
	 * @return skillData.size()
	 */
	public int size() {
		return skillData.size();
	}

	/**
	 * @return the skillTemplates
	 */
	public List<SkillTemplate> getSkillTemplates() {
		return skillTemplates;
	}

	/**
	 * @param skillTemplates
	 *          the skillTemplates to set
	 */
	public void setSkillTemplates(List<SkillTemplate> skillTemplates) {
		this.skillTemplates = skillTemplates;
		afterUnmarshal(null, null);
	}

	/**
	 * This method creates a HashMap with all skills assigned to their representative cooldownIds
	 */
	public void initializeCooldownGroups() {
		cooldownGroups = new HashMap<Integer, ArrayList<Integer>>();
		for (SkillTemplate skillTemplate : skillTemplates) {
			int cooldownId = skillTemplate.getCooldownId();
			if (!cooldownGroups.containsKey(cooldownId)) {
				cooldownGroups.put(cooldownId, new ArrayList<Integer>());
			}
			cooldownGroups.get(cooldownId).add(skillTemplate.getSkillId());
		}
	}

	/**
	 * This method is used to get all skills assigned to a specific cooldownId
	 * 
	 * @param cooldownId
	 * @return ArrayList<Integer> including all skills for asked cooldownId
	 */
	public ArrayList<Integer> getSkillsForCooldownId(int cooldownId) {
		if (cooldownGroups == null)
			initializeCooldownGroups();
		return cooldownGroups.get(cooldownId);
	}
}
