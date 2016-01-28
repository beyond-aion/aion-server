package com.aionemu.gameserver.model.templates.item;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import javolution.util.FastTable;

import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.skillengine.model.SkillTemplate;

/**
 * @author ATracer
 * @modified Neon
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = "Stigma")
public class Stigma {

	@XmlAttribute(name = "gain_skill_group1")
	protected String gainSkillGroup1;

	@XmlAttribute(name = "gain_skill_group2")
	protected String gainSkillGroup2;

	@XmlAttribute(name = "chargeable")
	protected boolean chargeable;

	public List<SkillTemplate> getGroupSkillTemplates(int groupNo) {
		List<SkillTemplate> skills = new FastTable<>();
		if ((groupNo == 0 || groupNo == 1) && gainSkillGroup1 != null) {
			for (SkillTemplate st : DataManager.SKILL_DATA.getSkillTemplate(gainSkillGroup1))
				skills.add(st);
		}
		if ((groupNo == 0 || groupNo == 2) && gainSkillGroup2 != null) {
			for (SkillTemplate st : DataManager.SKILL_DATA.getSkillTemplate(gainSkillGroup2))
				skills.add(st);
		}
		return skills;
	}

	public List<SkillTemplate> getAllSkillTemplates() {
		return getGroupSkillTemplates(0);
	}

	public boolean isChargeable() {
		return chargeable;
	}
}
