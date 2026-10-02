package com.aionemu.gameserver.skillengine.effect;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

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
	@XmlAttribute(name = "chance_delta")
	private int chanceDelta;
	@XmlAttribute(name = "addeffect")
	private boolean addEffect = false;

	/**
	 * @return the skillId
	 */
	public int getSkillId() {
		return skillId;
	}

	public int getChance(int skillLevel) {
		return chance + chanceDelta * skillLevel;
	}

	/**
	 * @return the addEffect
	 */
	public boolean isAddEffect() {
		return addEffect;
	}

}
