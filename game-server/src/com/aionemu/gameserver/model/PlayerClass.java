package com.aionemu.gameserver.model;

import javax.xml.bind.annotation.XmlEnum;

import com.aionemu.gameserver.model.templates.L10n;

/**
 * This enum represent class that a player may belong to.
 * 
 * @author Luno
 */
@XmlEnum
public enum PlayerClass implements L10n {
	WARRIOR(0, 240000, true),
	GLADIATOR(1, 240001, WARRIOR), // fighter
	TEMPLAR(2, 240002, WARRIOR), // knight
	SCOUT(3, 240003, true),
	ASSASSIN(4, 240004, SCOUT),
	RANGER(5, 240005, SCOUT),
	MAGE(6, 240006, true),
	SORCERER(7, 240007, MAGE), // wizard
	SPIRIT_MASTER(8, 240008, MAGE), // elementalist
	PRIEST(9, 240009, true),
	CLERIC(10, 240010, PRIEST),
	CHANTER(11, 240011, PRIEST),
	ENGINEER(12, 904314, true),
	RIDER(13, 904315, ENGINEER),
	GUNNER(14, 904316, ENGINEER),
	ARTIST(15, 904317, true),
	BARD(16, 904318, ARTIST),
	ALL(17);

	/** This id is used on client side */
	private final byte classId;

	private final int nameId;

	/** This is the mask for this class id, used with bitwise AND in arguments that contain more than one possible class */
	private final int idMask;

	/** Tells whether player can create new character with this class */
	private PlayerClass startingClass;

	PlayerClass(int classId) {
		this(classId, 0, false);
	}

	PlayerClass(int classId, int nameId, PlayerClass startingClass) {
		this(classId, nameId, false);
		this.startingClass = startingClass;
	}

	PlayerClass(int classId, int nameId, boolean isStartingClass) {
		this.classId = (byte) classId;
		this.nameId = nameId;
		this.idMask = (int) Math.pow(2, classId);
		if (isStartingClass)
			this.startingClass = this;
	}

	/**
	 * Returns client-side id for this PlayerClass
	 * 
	 * @return classID
	 */
	public byte getClassId() {
		return classId;
	}

	/**
	 * Returns <tt>PlayerClass</tt> object correlating with given classId.
	 * 
	 * @param classId
	 *          - id of player class
	 * @return PlayerClass objects that matches the given classId. If there isn't any objects that matches given id, then
	 *         <b>IllegalArgumentException</b> is being thrown.
	 */
	public static PlayerClass getPlayerClassById(byte classId) {
		for (PlayerClass pc : values()) {
			if (pc.getClassId() == classId)
				return pc;
		}

		throw new IllegalArgumentException("There is no player class with id " + classId);
	}

	public int getL10nId() {
		return nameId;
	}

	/**
	 * @return true if this is one of starting classes ( player can create char with this class )
	 */
	public boolean isStartingClass() {
		return startingClass == this;
	}

	public PlayerClass getStartingClass() {
		return startingClass;
	}

	public int getMask() {
		return idMask;
	}

	public boolean isPhysicalClass() {
		switch (this) {
			case WARRIOR:
			case GLADIATOR:
			case TEMPLAR:
			case SCOUT:
			case ASSASSIN:
			case RANGER:
			case CHANTER:
				return true;
			default:
				return false;
		}
	}
}
