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
	@XmlAttribute(name = "npc_id")
	protected int npc_id = 0;
	@XmlAttribute(name = "delay")
	protected int delay = 0;
	@XmlAttribute(name = "distance")
	protected int distance = 0;
	@XmlAttribute(name = "direction")
	protected float direction;
	
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
	
	/**
	 * @return npc_id
	 */
	public int getNpcId() {
		return npc_id;
	}

	/**
	 * @return delay
	 */
	public int getDelay() {
		return delay;
	}
	
	/**
	 * @return distance
	 */
	public int getDistance() {
		return distance;
	}
	
	/**
	 * @return direction
	 */
	public float getDirection() {
		return direction;
	}
}
