package com.aionemu.gameserver.skillengine.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.PlayerClass;
import com.aionemu.gameserver.model.Race;

/**
 * @author ATracer
 * @modified Neon
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "skill")
public class SkillLearnTemplate {

	@XmlAttribute(name = "classId", required = true)
	private PlayerClass classId = PlayerClass.ALL;
	@XmlAttribute(name = "skillId", required = true)
	private int skillId;
	@XmlAttribute(name = "skillLearn", required = false)
	private Integer skillLearn;
	@XmlAttribute(name = "race", required = true)
	private Race race;
	@XmlAttribute(name = "minLevel", required = true)
	private int minLevel;
	@XmlAttribute
	private boolean autolearn;
	@XmlAttribute
	private byte stigma = 0;

	/**
	 * @return the classId
	 */
	public PlayerClass getClassId() {
		return classId;
	}

	/**
	 * @return the skillId
	 */
	public int getSkillId() {
		return skillId;
	}

	/**
	 * @return the skill level
	 */
	public int getSkillLevel() {
		return DataManager.SKILL_DATA.getSkillTemplate(skillId).getLvl();
	}

	/**
	 * @return the skill stack name
	 */
	public String getStack() {
		return DataManager.SKILL_DATA.getSkillTemplate(skillId).getStack();
	}

	/**
	 * @return the minLevel
	 */
	public int getMinLevel() {
		return minLevel;
	}

	/**
	 * @return the race
	 */
	public Race getRace() {
		return race;
	}

	/**
	 * @return the autolearn
	 */
	public boolean isAutolearn() {
		return autolearn;
	}

	/**
	 * @return the stigma
	 */
	public boolean isStigma() {
		return stigma > 0;
	}

	public boolean isLinkedStigma() {
		return stigma == 4;
	}

	/**
	 * Skill learning since 4.8 is different than before. Every level of a skill has its own skillId.<br>
	 * This method returns the skillId of the next lower level of a skill.
	 * 
	 * @return The skillId of the pre-skill to this one or {@code null} if it has no pre-skill.
	 */
	public Integer getLearnSkill() {
		return skillLearn;
	}
}
