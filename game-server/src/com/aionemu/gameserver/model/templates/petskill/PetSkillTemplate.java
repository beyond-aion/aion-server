package com.aionemu.gameserver.model.templates.petskill;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

/**
 * @author ATracer
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "pet_skill")
public class PetSkillTemplate {

	@XmlAttribute(name = "skill_id")
	protected int skillId;
	@XmlAttribute(name = "pet_id")
	protected int petId;
	@XmlAttribute(name = "order_skill")
	protected int orderSkill;

	/**
	 * @return the skillId
	 */
	public int getSkillId() {
		return skillId;
	}

	/**
	 * @return the petId
	 */
	public int getPetId() {
		return petId;
	}

	/**
	 * @return the orderSkill
	 */
	public int getOrderSkill() {
		return orderSkill;
	}
}
