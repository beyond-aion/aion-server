package com.aionemu.gameserver.model.team.common.events;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author ATracer
 */
public enum TeamCommand {

	GROUP_BAN_MEMBER(2),
	GROUP_SET_LEADER(3),
	GROUP_REMOVE_MEMBER(6),
	GROUP_SET_LFG(9), // TODO confirm
	GROUP_START_MENTORING(10),
	GROUP_END_MENTORING(11),
	ALLIANCE_LEAVE(14),
	ALLIANCE_BAN_MEMBER(16),
	ALLIANCE_SET_CAPTAIN(17),
	ALLIANCE_CHECKREADY_CANCEL(20),
	ALLIANCE_CHECKREADY_START(21),
	ALLIANCE_CHECKREADY_AUTOCANCEL(22),
	ALLIANCE_CHECKREADY_READY(23),
	ALLIANCE_CHECKREADY_NOTREADY(24),
	ALLIANCE_SET_VICECAPTAIN(25),
	ALLIANCE_UNSET_VICECAPTAIN(26),
	ALLIANCE_CHANGE_GROUP(27),
	LEAGUE_LEAVE(29),
	LEAGUE_EXPEL(30),
	LEAGUE_ALLIANCE_MOVE(31),
	LEAGUE_SET_LEADER(32);

	private static final Map<Integer, TeamCommand> teamCommands = new HashMap<>();

	static {
		for (TeamCommand eventCode : values()) {
			teamCommands.put(eventCode.getCodeId(), eventCode);
		}
	}

	private final int commandCode;

	private TeamCommand(int commandCode) {
		this.commandCode = commandCode;
	}

	public int getCodeId() {
		return commandCode;
	}

	public static final TeamCommand getCommand(int commandCode) {
		TeamCommand command = teamCommands.get(commandCode);
		Objects.requireNonNull(command, "Invalid team command code " + commandCode);
		return command;
	}

}
