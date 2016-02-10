package com.aionemu.gameserver.dataholders;

import gnu.trove.map.hash.TIntObjectHashMap;

import java.util.List;
import java.util.Map;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import javolution.util.FastMap;
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
	@XmlTransient
	private TIntObjectHashMap<SkillTemplate> skillData = new TIntObjectHashMap<>();

	@XmlTransient
	private Map<String, List<SkillTemplate>> skillTemplatesByGroups = new FastMap<>();

	/**
	 * Map that contains cooldownId - skillId List
	 */
	@XmlTransient
	private TIntObjectHashMap<List<Integer>> cooldownGroups = new TIntObjectHashMap<>();

	void afterUnmarshal(Unmarshaller u, Object parent) {
		skillData.clear();
		skillTemplatesByGroups.clear();
		cooldownGroups.clear();
		for (SkillTemplate skillTemplate : skillTemplates) {
			int skillId = skillTemplate.getSkillId();
			int cooldownId = skillTemplate.getCooldownId();
			skillData.put(skillId, skillTemplate);
			if (!cooldownGroups.containsKey(cooldownId))
				cooldownGroups.put(cooldownId, new FastTable<>());
			cooldownGroups.get(cooldownId).add(skillId);

			if (skillTemplate.getGroup() != null) {
				if (!skillTemplatesByGroups.containsKey(skillTemplate.getGroup()))
					skillTemplatesByGroups.put(skillTemplate.getGroup(), new FastTable<>());
				skillTemplatesByGroups.get(skillTemplate.getGroup()).add(skillTemplate);
			}
		}
	}

	/**
	 * @param skillId
	 * @return SkillTemplate
	 */
	public SkillTemplate getSkillTemplate(int skillId) {
		return skillData.get(skillId);
	}

	public List<SkillTemplate> getSkillTemplate(String skillGroup) {
		return skillTemplatesByGroups.get(skillGroup);
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
