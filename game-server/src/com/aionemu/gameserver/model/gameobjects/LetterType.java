package com.aionemu.gameserver.model.gameobjects;

/**
 * @author ginho1
 */
public enum LetterType {
	NORMAL(0),
	EXPRESS(1),
	BLACKCLOUD(2);

	private LetterType(int id) {
		this.id = id;
	}

	private int id;

	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}

	public static LetterType getLetterTypeById(int id) {
		for (LetterType lt : values()) {
			if (lt.id == id)
				return lt;
		}
		throw new IllegalArgumentException("Unsupported revive type: " + id);
	}
}
