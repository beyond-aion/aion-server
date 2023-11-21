package com.aionemu.gameserver.dataholders;

import java.util.*;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.*;

import com.aionemu.gameserver.model.PlayerClass;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.templates.Guides.GuideTemplate;

/**
 * @author xTz
 */
@XmlRootElement(name = "guides")
@XmlAccessorType(XmlAccessType.FIELD)
public class GuideHtmlData {

	@XmlElement(name = "guide", type = GuideTemplate.class)
	private List<GuideTemplate> guideTemplates;

	@XmlTransient
	private final Map<Integer, List<GuideTemplate>> templates = new HashMap<>();
	private final int CLASS_ALL = 255;

	void afterUnmarshal(Unmarshaller u, Object parent) {
		for (GuideTemplate template : guideTemplates) {
			addTemplate(template);
		}
		guideTemplates = null;
	}

	private void addTemplate(GuideTemplate template) {
		Race race = template.getRace();
		if (race == null)
			race = Race.PC_ALL;
		int classId = template.getPlayerClass() == null ? CLASS_ALL : template.getPlayerClass().ordinal();

		int hash = makeHash(classId, race.ordinal(), template.getLevel());
		List<GuideTemplate> value = templates.get(hash);
		if (value == null) {
			value = new ArrayList<>();
			templates.put(hash, value);
		}
		value.add(template);
	}

	public int size() {
		return templates.size();
	}

	public Map<Integer, List<GuideTemplate>> getTemplates() {
		return templates;
	}

	public GuideTemplate getTemplateByTitle(String title) {
		return templates.values().stream().flatMap(Collection::stream).filter(t -> t.getTitle().equals(title)).findFirst().orElse(null);
	}

	public GuideTemplate[] getTemplatesFor(PlayerClass playerClass, Race race, int level) {
		List<GuideTemplate> guideTemplate = new ArrayList<>();

		List<GuideTemplate> classRaceSpecificTemplates = templates.get(makeHash(playerClass.ordinal(), race.ordinal(), level));
		List<GuideTemplate> classSpecificTemplates = templates.get(makeHash(playerClass.ordinal(), Race.PC_ALL.ordinal(), level));
		List<GuideTemplate> raceSpecificTemplates = templates.get(makeHash(CLASS_ALL, race.ordinal(), level));
		List<GuideTemplate> generalTemplates = templates.get(makeHash(CLASS_ALL, Race.PC_ALL.ordinal(), level));

		if (classRaceSpecificTemplates != null)
			guideTemplate.addAll(classRaceSpecificTemplates);
		if (classSpecificTemplates != null)
			guideTemplate.addAll(classSpecificTemplates);
		if (raceSpecificTemplates != null)
			guideTemplate.addAll(raceSpecificTemplates);
		if (generalTemplates != null)
			guideTemplate.addAll(generalTemplates);

		return guideTemplate.toArray(new GuideTemplate[guideTemplate.size()]);
	}

	private static int makeHash(int classType, int race, int level) {
		int result = classType << 8;
		result = (result | race) << 8;
		return result | level;
	}
}
