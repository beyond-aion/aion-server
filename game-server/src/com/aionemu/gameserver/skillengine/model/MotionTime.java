package com.aionemu.gameserver.skillengine.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.model.Gender;
import com.aionemu.gameserver.model.Race;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "motion_time", propOrder = { "am", "af", "em", "ef" })
public class MotionTime {

	private Times am;
	private Times af;
	private Times em;
	private Times ef;

	@XmlAttribute(required = true)
	private String name; // TODO enum

	public String getName() {
		return name;
	}

	/**
	 * @return the am
	 */
	public Times getAm() {
		return am;
	}

	/**
	 * @param am
	 *          the am to set
	 */
	public void setAm(Times am) {
		this.am = am;
	}

	/**
	 * @return the af
	 */
	public Times getAf() {
		return af;
	}

	/**
	 * @param af
	 *          the af to set
	 */
	public void setAf(Times af) {
		this.af = af;
	}

	/**
	 * @return the em
	 */
	public Times getEm() {
		return em;
	}

	/**
	 * @param em
	 *          the em to set
	 */
	public void setEm(Times em) {
		this.em = em;
	}

	/**
	 * @return the ef
	 */
	public Times getEf() {
		return ef;
	}

	/**
	 * @param ef
	 *          the ef to set
	 */
	public void setEf(Times ef) {
		this.ef = ef;
	}

	/**
	 * @param name
	 *          the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	public boolean isAllZero() {
		return getAm().isAllZero() && getAf().isAllZero() && getEm().isAllZero() && getEf().isAllZero();
	}

	public Times getTimes(Race race, Gender gender) {
		switch (race) {
			case ASMODIANS:
				if (gender == Gender.MALE)
					return getAm();
				else
					return getAf();
			case ELYOS:
				if (gender == Gender.MALE)
					return getEm();
				else
					return getEf();

		}
		return null;
	}

	public int getTimeForWeapon(Race race, Gender gender, WeaponTypeWrapper weapon) {
		return getTimes(race, gender).getTimeForWeapon(weapon);
	}
}
