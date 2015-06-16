package com.aionemu.gameserver.model.templates.staticdoor;

import java.util.EnumSet;

/**
 * @author Rolandas
 */
public enum StaticDoorState {
	NONE(0),
	OPENED(1 << 0),
	CLICKABLE(1 << 1),
	CLOSEABLE(1 << 2),
	ONEWAY(1 << 3);

	private StaticDoorState(int flag) {
		this.flag = flag;
	}

	private int flag;

	public int getFlag() {
		return flag;
	}

	public static void setStates(int flags, EnumSet<StaticDoorState> state) {
		for (StaticDoorState states : StaticDoorState.values()) {
			if (states == NONE)
				continue;
			if ((flags & states.flag) == 0)
				state.remove(states);
			else
				state.add(states);
		}
	}

	public static int getFlags(EnumSet<StaticDoorState> doorStates) {
		int result = 0;
		for (StaticDoorState state : StaticDoorState.values()) {
			if (state == NONE)
				continue;
			if (doorStates.contains(state))
				result |= state.flag;
		}
		return result;
	}
}
