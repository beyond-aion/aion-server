package com.aionemu.gameserver.model;

import java.util.HashMap;
import java.util.Map;

/**
 * Receivers of data a client broadcasts to other clients (house scripts and team UI data).
 */
public enum ClientBroadcastType {

	TEAM(0),
	/**
	 * Everyone inside the house the sender stands on.
	 */
	HOUSE(1),
	/**
	 * Everyone on the house land the sender stands on.
	 */
	HOUSE_LAND(2),
	/**
	 * Like {@link #HOUSE}, but taken without checking that the sender is on a house land.
	 */
	HOUSE_UNCHECKED(3);

	private static final Map<Integer, ClientBroadcastType> types = new HashMap<>();

	static {
		for (ClientBroadcastType type : values())
			types.put(type.getId(), type);
	}

	private final int id;

	ClientBroadcastType(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}

	/**
	 * @return The type with the given id, null if there is none.
	 */
	public static ClientBroadcastType getById(int id) {
		return types.get(id);
	}
}
