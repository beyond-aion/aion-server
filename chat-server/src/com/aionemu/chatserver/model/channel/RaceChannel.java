package com.aionemu.chatserver.model.channel;

import com.aionemu.chatserver.model.ChannelType;
import com.aionemu.chatserver.model.Race;

/**
 * @author ATracer
 */
public abstract class RaceChannel extends Channel {

	protected Race race;

	public RaceChannel(ChannelType channelType, Race race, String identifier) {
		super(channelType, identifier);
		this.race = race;
	}

	/**
	 * @return the race
	 */
	public Race getRace() {
		return race;
	}
}
