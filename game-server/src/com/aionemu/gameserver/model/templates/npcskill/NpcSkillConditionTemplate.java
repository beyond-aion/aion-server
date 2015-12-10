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
	@XmlAttribute(name = "range")
	protected int range = 10;
	@XmlAttribute(name = "hpBelow")
	protected int hpBelow = 50;
	
	/**
	 * @return the condType
	 */
	public NpcSkillCondition getCondType() {
		return condType;
	}
	
	/**
	 * @return the range
	 */
	public int getRange() {
		return range;
	}
	
	/**
	 * @return the hpBelow
	 */
	public int getHpBelow() {
		return hpBelow;
	}
	
	
}
