package com.aionemu.gameserver.model.house;

/**
 * Commands of the house edit and renovation modes, as used by CM_HOUSE_EDIT and SM_HOUSE_EDIT.
 */
public enum HousingCommand {

	BEGIN_EDIT(1),
	END_EDIT(2),
	/**
	 * Moves an item from the inventory into the house.
	 */
	REGISTER_ITEM(3),
	/**
	 * Deletes a registered, unplaced item.
	 */
	REMOVE_ITEM(4),
	PUT_OBJECT(5),
	MOVE_OBJECT(6),
	/**
	 * Takes a placed object back into the list of registered items.
	 */
	WITHDRAW_OBJECT(7),
	BEGIN_BUILDING_EDIT(14),
	END_BUILDING_EDIT(15),
	CHANGE_BUILDING(16);

	private final int id;

	HousingCommand(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}

	/**
	 * @return The value with the given id, null if there is none.
	 */
	public static HousingCommand getById(int id) {
		for (HousingCommand value : values())
			if (value.id == id)
				return value;
		return null;
	}
}
