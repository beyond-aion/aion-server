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
		Race race = template.getRace();
		if (race == null)
			race = Race.PC_ALL;

		int hash = makeHash(template.getClassId().ordinal(), race.ordinal(), template.getMinLevel());
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
	 * Perform search for: - class specific skills (race = ALL) - class and race specific skills - non-specific skills (race = ALL, class = ALL)
	 * 
	 * @param playerClass
	 * @param level
	 * @param race
	 * @return SkillLearnTemplate[]
	 */
	public SkillLearnTemplate[] getTemplatesFor(PlayerClass playerClass, int level, Race race) {
		List<SkillLearnTemplate> newSkills = new FastTable<>();

		List<SkillLearnTemplate> classRaceSpecificTemplates = templates.get(makeHash(playerClass.ordinal(), race.ordinal(), level));
		List<SkillLearnTemplate> classSpecificTemplates = templates.get(makeHash(playerClass.ordinal(), Race.PC_ALL.ordinal(), level));
		List<SkillLearnTemplate> generalTemplates = templates.get(makeHash(PlayerClass.ALL.ordinal(), Race.PC_ALL.ordinal(), level));

		if (classRaceSpecificTemplates != null)
			newSkills.addAll(classRaceSpecificTemplates);
		if (classSpecificTemplates != null)
			newSkills.addAll(classSpecificTemplates);
		if (generalTemplates != null)
			newSkills.addAll(generalTemplates);

		return newSkills.toArray(new SkillLearnTemplate[newSkills.size()]);
	}

	public SkillLearnTemplate[] getTemplatesForSkill(int skillId) {
		List<SkillLearnTemplate> searchSkills = new FastTable<>();

		List<SkillLearnTemplate> byId = templatesById.get(skillId);
		if (byId != null)
			searchSkills.addAll(byId);

		return searchSkills.toArray(new SkillLearnTemplate[searchSkills.size()]);
	}
	
	public SkillLearnTemplate[] getStigmaSkillsForSkill(int skillId, PlayerClass playerClass, int level, Race race) {
		List<SkillLearnTemplate> addList = new FastTable<>();
		try {
			List<SkillLearnTemplate> byId = templatesById.get(skillId);
			if (!playerClass.isStartingClass()) {
				SkillLearnTemplate topTemplate = null;
				for (SkillLearnTemplate template : byId) {
					if (!template.isStigma())
						continue;
					if (template.getRace() != race && template.getRace() != Race.PC_ALL)
						continue;
					if (template.getClassId() != playerClass && template.getClassId() != PlayerClass.ALL)
						continue;
					topTemplate = template;
					break;
				}
				if (topTemplate != null)
					createSkillTree(topTemplate, addList, true);
			}
			for (int i = addList.size() - 1; i >= 0; i--) {
				SkillLearnTemplate treeEntry = addList.get(i);
				if (treeEntry.getMinLevel() > level)
					addList.remove(i);
			}
		}
		catch(Exception e) {
			//log.error("Error while getting learn templates for skill "+skillId+", skillLevel");
			//FIXME: NEED TO REPARSE skill_tree.xml
		}
		return addList.toArray(new SkillLearnTemplate[addList.size()]);
	}

	/**
	 * Creates skill tree list in ascending player level order recursively
	 * @param topSkill - max skill for the stigma specified in item templates
	 * @param addList - initially an empty list to which skills are added
	 * @param isStigma - creating stigma skill tree ?
	 */
	private void createSkillTree(SkillLearnTemplate topSkill, List<SkillLearnTemplate> addList, boolean isStigma) {
		if (isStigma != topSkill.isStigma())
			return;
		addList.add(0, topSkill);
		if (topSkill.getLearnSkill() == null)
			return;

		PlayerClass playerClass = topSkill.getClassId();
		Race race = topSkill.getRace();

		List<SkillLearnTemplate> byId = templatesById.get(topSkill.getLearnSkill());
		for (SkillLearnTemplate template : byId) {
			if (isStigma != template.isStigma())
				continue;
			if (template.getRace() != race && template.getRace() != Race.PC_ALL)
				continue;
			// TODO: if needed, it should check starting class as well. Then add another argument to the method
			if (template.getClassId() != playerClass && template.getClassId() != PlayerClass.ALL)
				continue;
			createSkillTree(template, addList, isStigma);
			break;
		}
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
