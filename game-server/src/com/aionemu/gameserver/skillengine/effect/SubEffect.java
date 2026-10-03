package com.aionemu.gameserver.skillengine.effect;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlType;

/**
 * @author ATracer
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SubEffect")
public class SubEffect {

	@XmlAttribute(name = "skill_id", required = true)
	private int skillId;
	@XmlAttribute
	private int chance = 100;
	@XmlAttribute(name = "addeffect")
	private boolean addEffect = false;

	/**
	 * @return the skillId
	 */
	public int getSkillId() {
		return skillId;
	}

	/**
	 * @return the chance
	 */
	public int getChance() {
		return chance;
	}

	/**
	 * @return the addEffect
	 */
	public boolean isAddEffect() {
		return addEffect;
	}

}
