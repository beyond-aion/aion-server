package com.aionemu.gameserver.dataholders;

import gnu.trove.map.hash.TIntObjectHashMap;

import java.util.List;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import javolution.util.FastTable;

import com.aionemu.gameserver.skillengine.model.SkillTemplate;

/**
 * @author ATracer
 * @modified Neon
 */
@XmlRootElement(name = "skill_data")
@XmlAccessorType(XmlAccessType.FIELD)
public class SkillData {

	@XmlElement(name = "skill_template")
	private List<SkillTemplate> skillTemplates;

	/**
	 * Map that contains skillId - SkillTemplate key-value pair
	 */
	private TIntObjectHashMap<SkillTemplate> skillData = new TIntObjectHashMap<>();

	/**
	 * Map that contains cooldownId - skillId List
	 */
	private TIntObjectHashMap<List<Integer>> cooldownGroups = new TIntObjectHashMap<>();

	void afterUnmarshal(Unmarshaller u, Object parent) {
		skillData.clear();
		cooldownGroups.clear();
		for (SkillTemplate skillTemplate : skillTemplates) {
			int skillId = skillTemplate.getSkillId();
			int cooldownId = skillTemplate.getCooldownId();
			skillData.put(skillId, skillTemplate);
			if (!cooldownGroups.containsKey(cooldownId)) {
				cooldownGroups.put(cooldownId, new FastTable<>());
			}
			cooldownGroups.get(cooldownId).add(skillId);
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
	 * This method is used to get all skills assigned to a specific cooldownId
	 * 
	 * @param cooldownId
	 * @return List including all skills for asked cooldownId
	 */
	public List<Integer> getSkillsForCooldownId(int cooldownId) {
		return cooldownGroups.get(cooldownId);
	}
}
