package com.aionemu.gameserver.model;

import javax.xml.bind.annotation.XmlEnum;

/**
 * Creature gender. Typically there are males and females. But who knows, maybe NC can invent something new ;)
 * 
 * @author SoulKeeper
 */
@XmlEnum
public enum Gender {
	/**
	 * Males
	 */
	MALE(0),

	/**
	 * Females
	 */
	FEMALE(1);

	/**
	 * id of gender
	 */
	private int genderId;

	/**
	 * Constructor.
	 * 
	 * @param genderId
	 *          id of the gender
	 */
	private Gender(int genderId) {
		this.genderId = genderId;
	}

	/**
	 * Get id of this gender.
	 * 
	 * @return gender id
	 */
	public int getGenderId() {
		return genderId;
	}
}
