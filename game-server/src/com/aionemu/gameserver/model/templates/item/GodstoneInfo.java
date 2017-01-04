package com.aionemu.gameserver.model.templates.item;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author ATracer
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = "Godstone")
public class GodstoneInfo {

	@XmlAttribute
	private int skillid;
	@XmlAttribute
	private int skilllvl;
	@XmlAttribute
	private int probability;
	@XmlAttribute
	private int probabilityleft;
	@XmlAttribute
	private int breakprob;
	@XmlAttribute
	private int nonbreakcount;

	/**
	 * @return the skillid
	 */
	public int getSkillId() {
		return skillid;
	}

	/**
	 * @return the skilllvl
	 */
	public int getSkillLevel() {
		return skilllvl;
	}

	/**
	 * @return the probability
	 */
	public int getProbability() {
		return probability;
	}

	/**
	 * @return the probabilityleft
	 */
	public int getProbabilityLeft() {
		return probabilityleft;
	}

	public int getBreakProb() {
		return breakprob;
	}

	public int getNonBreakCount() {
		return nonbreakcount;
	}

}
