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
	
	private List<Integer> chooseSkills() {
		List<Integer> skills = new FastTable<>();
		if(gainSkillGroup1 != null) {
			for (SkillTemplate st : DataManager.SKILL_DATA.getSkillTemplate(gainSkillGroup1)) {
				skills.add(st.getSkillId());
			}
		}
		if(gainSkillGroup2 != null) {
			for (SkillTemplate st : DataManager.SKILL_DATA.getSkillTemplate(gainSkillGroup2)) {
				skills.add(st.getSkillId());
			}
		}
		return skills;
	}

	/**
	 * Pass player data to calculate skill level
	 * @return list
	 */
	public List<StigmaSkill> getSkills(int lvl) {
		List<StigmaSkill> list = new FastTable<>();
		// linked stigma skills are not skills  who cannot 
		// be acquired by equip a stigma, they are skills added
		// or deleted automatically so isLinked Stigma can be 
		// set as false here
		for (Integer skillId : chooseSkills())
			list.add(new StigmaSkill(lvl, skillId, false));
		return list;
	}

	public boolean isChargeable() {
		return chargeable;
	}
}
