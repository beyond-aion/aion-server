package com.aionemu.gameserver.model.templates.flypath;

import java.util.HashMap;
import java.util.Map;

public enum WindstreamState {

	ENTER(0),
	ENTERED(1),
	LEAVE_GLIDING(2),
	LEAVE(3),
	/**
	 * Sent right after leaving, the position becomes the start of the next fall.
	 */
	LEFT(4),
	/**
	 * Drops the windstream progress without leaving it. The client is not known to send it.
	 */
	RESET(5),
	START_BOOST(7),
	END_BOOST(8);

	private static final Map<Integer, WindstreamState> states = new HashMap<>();

	static {
		for (WindstreamState state : values())
			states.put(state.getId(), state);
	}

	private final int id;

	WindstreamState(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}

	/**
	 * @return The state with the given id, null if there is none.
	 */
	public static WindstreamState getById(int id) {
		return states.get(id);
	}
}
