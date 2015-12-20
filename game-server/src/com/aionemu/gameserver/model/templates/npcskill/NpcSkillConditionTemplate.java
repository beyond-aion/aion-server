package com.aionemu.gameserver.model.templates.npcskill;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

/**
 * @author Yeats
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "cond")
public class NpcSkillConditionTemplate {

	@XmlAttribute(name = "condType")
	protected NpcSkillCondition condType = NpcSkillCondition.NONE;
	@XmlAttribute(name = "hpBelow")
	protected int hpBelow = 50;
	@XmlAttribute(name = "skill_id")
	protected int skillId;
	@XmlAttribute(name = "range")
	protected int range = 10;
	
	/**
	 * @return the condType
	 */
	public NpcSkillCondition getCondType() {
		return condType;
	}
	
	/**
	 * @return the hpBelow
	 */
	public int getHpBelow() {
		return hpBelow;
	}
	
	/**
	 * @return skillId
	 */
	public int getSkillId() {
		return skillId;
	}
	
	/**
	 * @return range
	 */
	public int getRange() {
		return range;
	}
}
