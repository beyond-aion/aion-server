package com.aionemu.gameserver.dataholders;

import gnu.trove.map.hash.TIntObjectHashMap;

import java.util.List;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import javolution.util.FastTable;

import com.aionemu.gameserver.model.PlayerClass;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.skillengine.model.SkillLearnTemplate;
import com.aionemu.gameserver.skillengine.model.SkillTemplate;

/**
 * @author ATracer
 */
@XmlRootElement(name = "skill_tree")
@XmlAccessorType(XmlAccessType.FIELD)
public class SkillTreeData {

	@XmlElement(name = "skill")
	private List<SkillLearnTemplate> skillTemplates;

	private final TIntObjectHashMap<List<SkillLearnTemplate>> templates = new TIntObjectHashMap<>();
	private final TIntObjectHashMap<List<SkillLearnTemplate>> templatesById = new TIntObjectHashMap<>();

	void afterUnmarshal(Unmarshaller u, Object parent) {
		for (SkillLearnTemplate template : skillTemplates)
			addTemplate(template);
		skillTemplates = null;
	}

	private void addTemplate(SkillLearnTemplate template) {
		int hash = makeHash(template.getClassId().ordinal(), template.getRace().ordinal(), template.getMinLevel());
		List<SkillLearnTemplate> value = templates.get(hash);
		if (value == null) {
			value = new FastTable<>();
			templates.put(hash, value);
		}

		value.add(template);

		value = templatesById.get(template.getSkillId());
		if (value == null) {
			value = new FastTable<>();
			templatesById.put(template.getSkillId(), value);
		}

		value.add(template);
	}

	/**
	 * @return the templates
	 */
	public TIntObjectHashMap<List<SkillLearnTemplate>> getTemplates() {
		return templates;
	}

	/**
	 * Perform search for all skill templates that match the given class, level and race.
	 * 
	 * @param playerClass
	 * @param level
	 * @param race
	 * @return SkillLearnTemplate[]
	 */
	public List<SkillLearnTemplate> getTemplatesFor(PlayerClass playerClass, int level, Race race) {
		List<SkillLearnTemplate> newSkills = new FastTable<>();

		List<SkillLearnTemplate> classRaceSpecificTemplates = templates.get(makeHash(playerClass.ordinal(), race.ordinal(), level));
		List<SkillLearnTemplate> classSpecificTemplates = templates.get(makeHash(playerClass.ordinal(), Race.PC_ALL.ordinal(), level));
		List<SkillLearnTemplate> raceSpecificTemplates = templates.get(makeHash(PlayerClass.ALL.ordinal(), race.ordinal(), level));
		List<SkillLearnTemplate> generalTemplates = templates.get(makeHash(PlayerClass.ALL.ordinal(), Race.PC_ALL.ordinal(), level));

		if (classRaceSpecificTemplates != null)
			newSkills.addAll(classRaceSpecificTemplates);
		if (classSpecificTemplates != null)
			newSkills.addAll(classSpecificTemplates);
		if (raceSpecificTemplates != null)
			newSkills.addAll(raceSpecificTemplates);
		if (generalTemplates != null)
			newSkills.addAll(generalTemplates);

		return newSkills;
	}

	/**
	 * @param skillId
	 * @param playerClass
	 * @param race
	 * @param maxLevel
	 * @return All skills that are of the same skill stack for this skill ID. Class and race are necessary but playerLevel can be set to -1 if you want
	 *         the full skill list. It's needed since every level of a certain skill has its own ID since 4.8. If you would only add the current skill
	 *         ID, you wouldn't be able to use the lower level versions of a skill.
	 */
	public List<SkillLearnTemplate> getSkillsForSkill(int skillId, PlayerClass playerClass, Race race, int playerLevel) {
		List<SkillLearnTemplate> skillTree = new FastTable<>();
		for (SkillLearnTemplate learnTemplate : getTemplatesForSkill(getHighestSkill(skillId), playerClass, race)) {
			createSkillTree(learnTemplate, skillTree);
			break;
		}
		if (playerLevel > -1)
			skillTree.removeIf(template -> template.getMinLevel() > playerLevel);
		return skillTree;
	}

	/**
	 * Creates skill tree list in ascending player level order recursively
	 * 
	 * @param topSkill
	 *          - max/best skill version of the stack
	 * @param addList
	 *          - initially an empty list to which skills are added
	 * @param isStigma
	 *          - creating stigma skill tree ?
	 */
	private void createSkillTree(SkillLearnTemplate topSkill, List<SkillLearnTemplate> addList) {
		if (topSkill == null)
			return;
		addList.add(0, topSkill);
		if (topSkill.getLearnSkill() == null)
			return;

		for (SkillLearnTemplate template : getTemplatesForSkill(topSkill.getLearnSkill(), topSkill.getClassId(), topSkill.getRace())) {
			if (topSkill.isStigma() != template.isStigma())
				continue;
			createSkillTree(template, addList);
			break;
		}
	}

	/**
	 * @param skillId
	 * @param playerClass
	 * @param race
	 * @return All skill learn templates with the specified skill ID, that match the players class and race. Should return 1 template max. If more, most
	 *         probably skill_tree.xml is parsed incorrectly.
	 */
	public List<SkillLearnTemplate> getTemplatesForSkill(int skillId, PlayerClass playerClass, Race race) {
		List<SkillLearnTemplate> searchSkills = new FastTable<>();
		List<SkillLearnTemplate> byId = templatesById.get(skillId);
		if (byId != null) {
			for (SkillLearnTemplate template : byId)
				if ((template.getClassId() == PlayerClass.ALL || template.getClassId() == playerClass)
					&& (template.getRace() == Race.PC_ALL || template.getRace() == race))
					searchSkills.add(template);
		}
		return searchSkills;
	}

	/**
	 * @param skillId
	 * @return The skill id with the highest skill level of this skills' stack.
	 */
	private int getHighestSkill(int skillId) {
		SkillTemplate baseTemplate = DataManager.SKILL_DATA.getSkillTemplate(skillId);
		if (baseTemplate == null)
			return skillId;
		List<SkillTemplate> templates = DataManager.SKILL_DATA.getSkillTemplatesByStack(baseTemplate.getStack());
		if (templates == null)
			return skillId;
		SkillTemplate highestSkill = templates.stream().max((template1, template2) -> template1.getLvl() - template2.getLvl()).orElse(baseTemplate);
		return highestSkill.getSkillId();
	}

	public boolean isLearnedSkill(int skillId) {
		return templatesById.get(skillId) != null;
	}

	public int size() {
		int size = 0;
		for (Integer key : templates.keys())
			size += templates.get(key).size();
		return size;
	}

	private static int makeHash(int classId, int race, int level) {
		int result = classId << 8;
		result = (result | race) << 8;
		return result | level;
	}
}
