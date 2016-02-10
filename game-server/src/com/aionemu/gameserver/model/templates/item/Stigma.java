package com.aionemu.gameserver.model.templates.item;

import java.util.List;
import java.util.Set;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.skillengine.model.SkillTemplate;

import javolution.util.FastSet;

/**
 * @author ATracer
 * @modified Neon
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = "Stigma")
public class Stigma {

	@XmlAttribute(name = "gain_skill_group1")
	private String gainSkillGroup1;

	@XmlAttribute(name = "gain_skill_group2")
	private String gainSkillGroup2;

	@XmlAttribute(name = "chargeable")
	private boolean chargeable;

	@XmlTransient
	private String[] gainSkillGroups;

	void afterUnmarshal(Unmarshaller u, Object parent) {
		Set<String> groups = FastSet.of(gainSkillGroup1, gainSkillGroup2);
		groups.removeIf(group -> group == null || group.isEmpty());
		gainSkillGroups = groups.toArray(new String[groups.size()]);
	}

	public String[] getGainSkillGroups() {
		return gainSkillGroups;
	}

	public List<SkillTemplate> getGainSkillsByGroup(int groupNo) {
		if (groupNo > 0 && groupNo <= gainSkillGroups.length)
			return DataManager.SKILL_DATA.getSkillTemplate(gainSkillGroups[groupNo - 1]);
		else
			return null;
	}

	public boolean isChargeable() {
		return chargeable;
	}
}
