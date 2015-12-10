package com.aionemu.gameserver.model;

import javax.xml.bind.annotation.XmlEnum;

/**
 * This enum represent class that a player may belong to.
 * 
 * @author Luno
 */
@XmlEnum
public enum PlayerClass {
	WARRIOR(0, true),
	GLADIATOR(1), // fighter
	TEMPLAR(2), // knight
	SCOUT(3, true),
	ASSASSIN(4),
	RANGER(5),
	MAGE(6, true),
	SORCERER(7), // wizard
	SPIRIT_MASTER(8), // elementalist
	PRIEST(9, true),
	CLERIC(10),
	CHANTER(11),
	ENGINEER(12, true),
	RIDER(13),
	GUNNER(14),
	ARTIST(15, true),
	BARD(16),
	ALL(17),
	
	PHYSICAL_CLASS(WARRIOR.getClassId() | GLADIATOR.getClassId() | TEMPLAR.getClassId() |
								SCOUT.getClassId() | ASSASSIN.getClassId() | RANGER.getClassId() | CHANTER.getClassId()),
	MAGICAL_CLASS(MAGE.getClassId() | SORCERER.getClassId() | SPIRIT_MASTER.getClassId() |  PRIEST.getClassId() | 
								CLERIC.getClassId() | ENGINEER.getClassId() | RIDER.getClassId() | GUNNER.getClassId() |
								ARTIST.getClassId() | BARD.getClassId());

	/** This id is used on client side */
	private byte classId;

	/** This is the mask for this class id, used with bitwise AND in arguments that contain more than one possible class */
	private int idMask;

	/** Tells whether player can create new character with this class */
	private boolean startingClass;

	private PlayerClass(int classId) {
		this(classId, false);
	}

	private PlayerClass(int classId, boolean startingClass) {
		this.classId = (byte) classId;
		this.startingClass = startingClass;
		this.idMask = (int) Math.pow(2, classId);
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

	/**
	 * @return true if this is one of starting classes ( player can create char with this class )
	 */
	public boolean isStartingClass() {
		return startingClass;
	}

	/**
	 * @param pc
	 * @return starting class for second class
	 */
	public static PlayerClass getStartingClassFor(PlayerClass pc) {
		PlayerClass[] vals = values();
		byte i = pc.getClassId();
		while (i >= 0) {
			if (vals[i].isStartingClass())
				return vals[i];
			i--;
		}
		throw new IllegalArgumentException("No Starting Class for PlayerClass: " + pc.toString());
	}

	public static PlayerClass getPlayerClassByString(String fieldName) {
		for (PlayerClass pc : values()) {
			if (pc.toString().equals(fieldName))
				return pc;
		}
		return null;
	}

	public int getMask() {
		return idMask;
	}
}
