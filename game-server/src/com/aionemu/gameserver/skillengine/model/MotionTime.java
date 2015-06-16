package com.aionemu.gameserver.skillengine.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.model.Gender;
import com.aionemu.gameserver.model.Race;


@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "motion_time", propOrder = {"am" , "af" , "em" , "ef"})
public class MotionTime {
	
	protected Times am;
	protected Times af;
	protected Times em;
	protected Times ef;
	
	@XmlAttribute(required = true)
	protected String name;// TODO enum

	
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
	 * @param am the am to set
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
	 * @param af the af to set
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
	 * @param em the em to set
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
	 * @param ef the ef to set
	 */
	public void setEf(Times ef) {
		this.ef = ef;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	public Times getTimes(Race race, Gender gender) {
		
		switch (race) {
			case ASMODIANS:
				if (gender == Gender.MALE)
					return this.getAm();
				else
					return this.getAf();
			case ELYOS:
				if (gender == Gender.MALE)
					return this.getEm();
				else
					return this.getEf();
				
		}
		
		return null;
	}

	public int getTimeForWeapon(Race race, Gender gender, WeaponTypeWrapper weapon) {
		
		switch (race) {
			case ASMODIANS:
				if (gender == Gender.MALE)
					return this.getAm().getTimeForWeapon(weapon);
				else
					return this.getAf().getTimeForWeapon(weapon);
			case ELYOS:
				if (gender == Gender.MALE)
					return this.getEm().getTimeForWeapon(weapon);
				else
					return this.getEf().getTimeForWeapon(weapon);
				
		}
		
		return 0;
	}

}
