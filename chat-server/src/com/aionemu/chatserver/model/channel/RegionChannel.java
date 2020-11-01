package com.aionemu.chatserver.model.channel;

import com.aionemu.chatserver.model.ChannelType;
import com.aionemu.chatserver.model.Race;

/**
 * @author ATracer, Neon
 */
public class RegionChannel extends RaceChannel {

	private final String mapIdentifier;

	public RegionChannel(int gameServerId, Race race, String mapIdentifier) {
		super(ChannelType.REGION, gameServerId, race);
		this.mapIdentifier = mapIdentifier;
	}

	public String getMapIdentifier() {
		return mapIdentifier;
	}

	@Override
	public boolean matches(ChannelType channelType, int gameServerId, Race race, String mapIdentifier) {
		return super.matches(channelType, gameServerId, race, mapIdentifier) && getMapIdentifier().equals(mapIdentifier);
	}
}
