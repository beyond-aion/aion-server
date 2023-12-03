package com.aionemu.gameserver.dataholders;

import java.util.*;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.*;

import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.skillengine.model.Motion;
import com.aionemu.gameserver.skillengine.model.MotionTime;
import com.aionemu.gameserver.skillengine.model.SkillTemplate;

/**
 * @author ATracer, Neon
 */
@XmlRootElement(name = "skill_data")
@XmlAccessorType(XmlAccessType.FIELD)
public class SkillData {

	@XmlElement(name = "skill_template")
	private List<SkillTemplate> skillTemplates;

	@XmlTransient
	private final Map<Integer, SkillTemplate> skillTemplateById = new HashMap<>();

	@XmlTransient
	private final Map<String, List<SkillTemplate>> skillTemplatesByGroup = new LinkedHashMap<>();

	@XmlTransient
	private final Map<String, List<SkillTemplate>> skillTemplatesByStack = new LinkedHashMap<>();

	@XmlTransient
	private final Map<Integer, List<Integer>> skillIdsByCooldownId = new HashMap<>();
	void afterUnmarshal(Unmarshaller u, Object parent) {
		skillTemplateById.clear();
		skillTemplatesByGroup.clear();
		skillTemplatesByStack.clear();
		skillIdsByCooldownId.clear();
		for (SkillTemplate skillTemplate : skillTemplates) {
			int skillId = skillTemplate.getSkillId();
			int cooldownId = skillTemplate.getCooldownId();
			skillTemplateById.put(skillId, skillTemplate);
			skillIdsByCooldownId.computeIfAbsent(cooldownId, k -> new ArrayList<>()).add(skillId);
			if (skillTemplate.getGroup() != null)
				skillTemplatesByGroup.computeIfAbsent(skillTemplate.getGroup(), k -> new ArrayList<>()).add(skillTemplate);
			if (skillTemplate.getStack() != null)
				skillTemplatesByStack.computeIfAbsent(skillTemplate.getStack(), k -> new ArrayList<>()).add(skillTemplate);
		}
		skillTemplates = null;
	}

	public SkillTemplate getSkillTemplate(int skillId) {
		return skillTemplateById.get(skillId);
	}

	/**
	 * @return All skill templates of this group. A group is less precise and may be null.
	 */
	public List<SkillTemplate> getSkillTemplatesByGroup(String skillGroup) {
		return skillTemplatesByGroup.get(skillGroup);
	}

	/**
	 * @return All skill templates of this stack. A stack is more precise than a group.
	 *         For example: Charge skills have their own stack per charging level. Also, skills that are similar for both factions have the same group,
	 *         but different stacks.
	 */
	public List<SkillTemplate> getSkillTemplatesByStack(String skillStack) {
		return skillTemplatesByStack.get(skillStack);
	}

	public int size() {
		return skillTemplateById.size();
	}

	public Collection<SkillTemplate> getSkillTemplates() {
		return skillTemplateById.values();
	}

	public List<Integer> getSkillsForCooldownId(int cooldownId) {
		return skillIdsByCooldownId.get(cooldownId);
	}

	public void validateMotions() {
		StringBuilder missing = new StringBuilder();
		Set<String> motionNames = new HashSet<>();
		for (SkillTemplate t : getSkillTemplates()) {
			Motion m = t.getMotion();
			if (m == null || m.getName() == null)
				continue;
			if (motionNames.add(m.getName())) {
				MotionTime mt = DataManager.MOTION_DATA.getMotionTime(m.getName());
				if (mt == null)
					missing.append('"').append(m.getName()).append("\" (skill id ").append(t.getSkillId()).append("), ");
			}
		}
		if (missing.length() > 0)
			LoggerFactory.getLogger(SkillData.class).warn("Missing motion times for these motion names: {}", missing.substring(0, missing.length() - 2));
	}
}
