package com.aionemu.gameserver.skillengine.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.model.PlayerClass;
import com.aionemu.gameserver.model.Race;

/**
 * @author ATracer
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
	@XmlAttribute(name = "name", required = true)
	private String name;
	@XmlAttribute(name = "race", required = true)
	private Race race;
	@XmlAttribute(name = "minLevel", required = true)
	private int minLevel;
	@XmlAttribute
	private boolean autolearn;
	@XmlAttribute
	private byte stigma = 0;
	
	@XmlTransient
	private int skillLevel = 1;

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
	 * @return the skillLevel
	 */
	public int getSkillLevel() {
		return skillLevel;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
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
	 * @return the skillLearn, null if learn is not needed
	 */
	public Integer getLearnSkill() {
		return skillLearn;
	}
}
