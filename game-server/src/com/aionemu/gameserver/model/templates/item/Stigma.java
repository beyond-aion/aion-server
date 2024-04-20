package com.aionemu.gameserver.model.templates.item;

import java.util.List;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.skillengine.model.SkillTemplate;

/**
 * @author ATracer, Neon
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = "Stigma")
public class Stigma {

	@XmlAttribute(name = "gain_skill_group1", required = true)
	private String gainSkillGroup1;

	@XmlAttribute(name = "gain_skill_group2")
	private String gainSkillGroup2;

	@XmlAttribute(name = "chargeable")
	private boolean chargeable;

	@XmlTransient
	private String[] gainSkillGroups;

	void afterUnmarshal(Unmarshaller u, Object parent) {
		if (gainSkillGroup2 == null)
			gainSkillGroups = new String[] { gainSkillGroup1 };
		else
			gainSkillGroups = new String[] { gainSkillGroup1, gainSkillGroup2 };
	}

	public String[] getGainSkillGroups() {
		return gainSkillGroups;
	}

	public List<SkillTemplate> getGainSkillsByGroup(int groupNo) {
		if (groupNo > 0 && groupNo <= gainSkillGroups.length)
			return DataManager.SKILL_DATA.getSkillTemplatesByGroup(gainSkillGroups[groupNo - 1]);
		else
			return null;
	}

	public boolean isChargeable() {
		return chargeable;
	}
}
