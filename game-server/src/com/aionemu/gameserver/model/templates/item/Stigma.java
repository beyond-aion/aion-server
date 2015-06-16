package com.aionemu.gameserver.model.templates.item;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author ATracer
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = "Stigma")
public class Stigma {

	@XmlElement(name = "require_skill")
	protected List<RequireSkill> requireSkill;
	@XmlAttribute
	protected List<String> skill;
	@XmlAttribute
	protected int shard;

	/**
	 * 
	 * @return list
	 */
	public List<StigmaSkill> getSkills() {
		List<StigmaSkill> list = new ArrayList<StigmaSkill>();
		for (String st : skill) {
			String[] array = st.split(":"); 
			list.add(new StigmaSkill(Integer.parseInt(array[0]),Integer.parseInt(array[1])));
		}
		
		return list;
	}
	
	/**
	 * @return the shard
	 */
	public int getShard() {
		return shard;
	}

	public List<RequireSkill> getRequireSkill() {
		if (requireSkill == null) {
			requireSkill = new ArrayList<RequireSkill>();
		}
		return this.requireSkill;
	}
	
	public class StigmaSkill {
		private int skillId;
		private int skillLvl;
		
		public StigmaSkill(int skillLvl, int skillId) {
			this.skillId = skillId;
			this.skillLvl = skillLvl;
		}
		
		public int getSkillLvl() {
			return this.skillLvl;
		}
		
		public int getSkillId() {
			return this.skillId;
		}
	}

}
