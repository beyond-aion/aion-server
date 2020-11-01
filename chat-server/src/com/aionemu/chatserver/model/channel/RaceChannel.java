package com.aionemu.chatserver.model.channel;

import com.aionemu.chatserver.model.ChannelType;
import com.aionemu.chatserver.model.Race;

/**
 * @author ATracer, Neon
 */
public abstract class RaceChannel extends Channel {

	private final Race race;

	public RaceChannel(ChannelType channelType, int gameServerId, Race race) {
		super(channelType, gameServerId);
		this.race = race;
	}

	public Race getRace() {
		return race;
	}

	@Override
	public boolean matches(ChannelType channelType, int gameServerId, Race race, String channelMeta) {
		return race == getRace() && channelType == getChannelType() && gameServerId == getGameServerId();
	}

	@Override
	public String name() {
		return getChannelType().name() + " (" + getRace().name().charAt(0) + ")";
	}
}
