package com.aionemu.chatserver.model.channel;

import com.aionemu.chatserver.model.ChannelType;
import com.aionemu.chatserver.model.PlayerClass;
import com.aionemu.chatserver.model.Race;

/**
 * @author ATracer
 * @modified Neon
 */
public class JobChannel extends RaceChannel {

	private final PlayerClass playerClass;

	public JobChannel(int gameServerId, Race race, PlayerClass playerClass) {
		super(ChannelType.JOB, gameServerId, race);
		this.playerClass = playerClass;
	}

	@Override
	public boolean matches(ChannelType channelType, int gameServerId, Race race, String classIdentifier) {
		return super.matches(channelType, gameServerId, race, classIdentifier) && PlayerClass.getClassByIdentifier(classIdentifier) == playerClass;
	}

	@Override
	public String name() {
		return playerClass + " (" + getRace().name().substring(0, 1) + ")";
	}
}
