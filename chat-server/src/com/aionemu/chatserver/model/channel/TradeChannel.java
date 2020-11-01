package com.aionemu.chatserver.model.channel;

import com.aionemu.chatserver.model.ChannelType;
import com.aionemu.chatserver.model.Race;

/**
 * @author ATracer, Neon
 */
public class TradeChannel extends RaceChannel {

	private final String mapIdentifier;

	public TradeChannel(int gameServerId, Race race, String mapIdentifier) {
		super(ChannelType.TRADE, gameServerId, race);
		this.mapIdentifier = mapIdentifier;
	}

	public String getMapIdentifier() {
		return mapIdentifier;
	}

	@Override
	public boolean matches(ChannelType channelType, int gameServerId, Race race, String mapIdentifier) {
		return getMapIdentifier().equals(mapIdentifier) && super.matches(channelType, gameServerId, race, mapIdentifier);
	}
}
