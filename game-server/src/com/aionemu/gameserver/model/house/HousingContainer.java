package com.aionemu.gameserver.model.house;

/**
 * Lists of registered but unused items of a house.
 */
public enum HousingContainer {

	OBJECTS(1),
	DECORATIONS(2);

	private final int id;

	HousingContainer(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}

	/**
	 * @return The value with the given id, null if there is none.
	 */
	public static HousingContainer getById(int id) {
		for (HousingContainer value : values())
			if (value.id == id)
				return value;
		return null;
	}
}
