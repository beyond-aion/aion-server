package com.aionemu.chatserver.model;

/**
 * @author ATracer
 */
public enum PlayerClass {
	WARRIOR(0),
	GLADIATOR(1),
	TEMPLAR(2),
	SCOUT(3),
	ASSASSIN(4),
	RANGER(5),
	MAGE(6),
	SORCERER(7),
	SPIRIT_MASTER(8),
	PRIEST(9),
	CLERIC(10),
	CHANTER(11),
	TECHNIST(12),
	GUNSLINGER(13),
	MUSE(14),
	SONGWEAVER(15),
	RIDER(16), // TODO
	ALL(16);

	private byte classId;

	/**
	 * @param classId
	 */
	private PlayerClass(int classId) {
		this.classId = (byte) classId;
	}

	/**
	 * @return classId
	 */
	public byte getClassId() {
		return classId;
	}
}
