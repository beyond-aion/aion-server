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
	private Map<String, List<SkillTemplate>> skillTemplatesByGroup = new FastMap<>();

	@XmlTransient
	private Map<String, List<SkillTemplate>> skillTemplatesByStack = new FastMap<>();

	/**
	 * Map that contains cooldownId - skillId List
	 */
	@XmlTransient
	private TIntObjectHashMap<List<Integer>> cooldownGroups = new TIntObjectHashMap<>();

	void afterUnmarshal(Unmarshaller u, Object parent) {
		skillData.clear();
		skillTemplatesByGroup.clear();
		skillTemplatesByStack.clear();
		cooldownGroups.clear();
		for (SkillTemplate skillTemplate : skillTemplates) {
			int skillId = skillTemplate.getSkillId();
			int cooldownId = skillTemplate.getCooldownId();
			skillData.put(skillId, skillTemplate);
			if (!cooldownGroups.containsKey(cooldownId))
				cooldownGroups.put(cooldownId, new FastTable<>());
			cooldownGroups.get(cooldownId).add(skillId);

			if (skillTemplate.getGroup() != null) {
				if (!skillTemplatesByGroup.containsKey(skillTemplate.getGroup()))
					skillTemplatesByGroup.put(skillTemplate.getGroup(), new FastTable<>());
				skillTemplatesByGroup.get(skillTemplate.getGroup()).add(skillTemplate);
			}
			if (skillTemplate.getStack() != null) {
				if (!skillTemplatesByStack.containsKey(skillTemplate.getGroup()))
					skillTemplatesByStack.put(skillTemplate.getGroup(), new FastTable<>());
				skillTemplatesByStack.get(skillTemplate.getGroup()).add(skillTemplate);
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

	/**
	 * @param skillGroup
	 * @return All skill templates of this group. A group is less precise and may be null.
	 */
	public List<SkillTemplate> getSkillTemplatesByGroup(String skillGroup) {
		return skillTemplatesByGroup.get(skillGroup);
	}

	/**
	 * @param skillStack
	 * @return All skill templates of this stack. A stack is more precise than a group.
	 *         For example: Charge skills have their own stack per charging level. Also, skills that are similar for both factions have the same group,
	 *         but different stacks.
	 */
	public List<SkillTemplate> getSkillTemplatesByStack(String skillStack) {
		return skillTemplatesByStack.get(skillStack);
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
