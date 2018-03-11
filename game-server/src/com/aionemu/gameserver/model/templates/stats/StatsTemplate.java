package com.aionemu.gameserver.model.templates.stats;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 * This class is only a container for Stats. Created on: 04.08.2009 14:59:10
 * 
 * @author Aquanox
 * @modified Estrayl, Neon
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "stats_template")
public class StatsTemplate {

	@XmlAttribute(name = "maxHp")
	private int maxHp;
	@XmlAttribute(name = "maxMp")
	private int maxMp;

	@XmlAttribute(name = "evasion")
	private int evasion;
	@XmlAttribute(name = "block")
	private int block;
	@XmlAttribute(name = "parry")
	private int parry;
	@XmlAttribute(name = "pdef")
	private int pdef;
	@XmlAttribute(name = "mdef")
	private int mdef;
	@XmlAttribute(name = "mresist")
	private int mresist;
	@XmlAttribute(name = "msup")
	private int msup;
	@XmlAttribute(name = "strike_resist")
	private int strikeResist;
	@XmlAttribute(name = "spell_resist")
	private int spellResist;

	@XmlAttribute(name = "attack")
	private int attack;
	@XmlAttribute(name = "accuracy")
	private int accuracy;
	@XmlAttribute(name = "pcrit")
	private int pcrit;

	@XmlAttribute(name = "matk")
	private int matk;
	@XmlAttribute(name = "macc")
	private int macc;
	@XmlAttribute(name = "mcrit")
	private int mcrit;
	@XmlAttribute(name = "mboost")
	private int magicBoost;

	@XmlElement
	private CreatureSpeeds speeds;

	@XmlTransient
	private int abnormalResistance;

	/* ======================================= */

	public int getMaxHp() {
		return maxHp;
	}

	public void setMaxHp(int maxHp) {
		this.maxHp = maxHp;
	}

	public int getMaxMp() {
		return maxMp;
	}

	public void setMaxMp(int maxMp) {
		this.maxMp = maxMp;
	}

	/* ======================================= */

	public int getEvasion() {
		return evasion;
	}

	public void setEvasion(int evasion) {
		this.evasion = evasion;
	}

	public int getBlock() {
		return block;
	}

	public void setBlock(int block) {
		this.block = block;
	}

	public int getParry() {
		return parry;
	}

	public void setParry(int parry) {
		this.parry = parry;
	}

	public int getPdef() {
		return pdef;
	}

	public void setPdef(int pdef) {
		this.pdef = pdef;
	}

	public int getMdef() {
		return mdef;
	}

	public int getMresist() {
		return mresist;
	}

	public void setMresist(int mresist) {
		this.mresist = mresist;
	}

	public int getMsup() {
		return msup;
	}

	public int getStrikeResist() {
		return strikeResist;
	}

	public void setStrikeResist(int strikeResist) {
		this.strikeResist = strikeResist;
	}

	public int getSpellResist() {
		return spellResist;
	}

	/* ======================================= */

	public int getAttack() {
		return attack;
	}

	public void setAttack(int attack) {
		this.attack = attack;
	}

	public int getAccuracy() {
		return accuracy;
	}

	public void setAccuracy(int accuracy) {
		this.accuracy = accuracy;
	}

	public int getPcrit() {
		return pcrit;
	}

	public void setPcrit(int pcrit) {
		this.pcrit = pcrit;
	}

	/* ======================================= */

	public int getMagicalAttack() {
		return matk;
	}

	public void setMagicalAttack(int matk) {
		this.matk = matk;
	}

	public int getMacc() {
		return macc;
	}

	public void setMacc(int macc) {
		this.macc = macc;
	}

	public int getMcrit() {
		return mcrit;
	}

	public void setMcrit(int mcrit) {
		this.mcrit = mcrit;
	}

	public int getMagicBoost() {
		return magicBoost;
	}

	/* ======================================= */

	public float getWalkSpeed() {
		return speeds == null ? 0 : speeds.getWalkSpeed();
	}

	public float getRunSpeed() {
		return speeds == null ? 0 : speeds.getRunSpeed();
	}

	public float getGroupWalkSpeed() {
		return speeds == null ? 0 : speeds.getGroupWalkSpeed();
	}

	public float getRunSpeedFight() {
		return speeds == null ? 0 : speeds.getRunSpeedFight();
	}

	public float getGroupRunSpeedFight() {
		return speeds == null ? 0 : speeds.getGroupRunSpeedFight();
	}

	public float getFlySpeed() {
		return speeds == null ? 0 : speeds.getFlySpeed();
	}

	public int getAbnormalResistance() {
		return abnormalResistance;
	}

	public void setAbnormalResistance(int abnormalResistance) {
		this.abnormalResistance = abnormalResistance;
	}
}
