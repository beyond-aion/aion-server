package com.aionemu.gameserver.model.templates.stats;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Luno
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "npc_stats_template")
public class NpcStatsTemplate extends StatsTemplate {

	@XmlAttribute(name = "pdef")
	private int pdef;
	@XmlAttribute(name = "mdef")
	private int mdef;
	@XmlAttribute(name = "mresist")
	private int mresist;
	@XmlAttribute(name = "crit")
	private int crit;
	@XmlAttribute(name = "accuracy")
	private int accuracy;
	@XmlAttribute(name = "power")
	private int power;
	@XmlAttribute(name = "maxXp")
	private int maxXp;

	@Override
	public float getGroupWalkSpeed() {
		return speeds == null ? 0 : speeds.getGroupWalkSpeed();
	}

	
	/**
	 * @param pdef the pdef to set
	 */
	public void setPdef(int pdef) {
		this.pdef = pdef;
	}

	
	/**
	 * @param mdef the mdef to set
	 */
	public void setMdef(int mdef) {
		this.mdef = mdef;
	}

	
	/**
	 * @param mresist the mresist to set
	 */
	public void setMresist(int mresist) {
		this.mresist = mresist;
	}

	
	/**
	 * @param crit the crit to set
	 */
	public void setCrit(int crit) {
		this.crit = crit;
	}

	
	/**
	 * @param accuracy the accuracy to set
	 */
	public void setAccuracy(int accuracy) {
		this.accuracy = accuracy;
	}

	
	/**
	 * @param maxXp the maxXp to set
	 */
	public void setMaxXp(int maxXp) {
		this.maxXp = maxXp;
	}

	@Override
	public float getRunSpeedFight() {
		return speeds == null ? 0 : speeds.getRunSpeedFight();
	}

	@Override
	public float getGroupRunSpeedFight() {
		return speeds == null ? 0 : speeds.getGroupRunSpeedFight();
	}

	/**
	 * @return the pdef
	 */
	public int getPdef() {
		return pdef;
	}

	/**
	 * @return the mdef
	 */
	public float getMdef() {
		return mdef;
	}

	/**
	 * @return the mresist
	 */
	public int getMresist() {
		return mresist;
	}

	/**
	 * @return the crit
	 */
	public float getCrit() {
		return crit;
	}

	/**
	 * @return the accuracy
	 */
	public float getAccuracy() {
		return accuracy;
	}

	/**
	 * @return the power
	 */
	public int getPower() {
		return power;
	}

	public void setPower(int power) {
		this.power = power;
	}

	/**
	 * @return the maxXp
	 */
	public int getMaxXp() {
		return maxXp;
	}

}
