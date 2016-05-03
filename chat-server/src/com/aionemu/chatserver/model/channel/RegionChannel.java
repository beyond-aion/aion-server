package com.aionemu.chatserver.model.channel;

import com.aionemu.chatserver.model.ChannelType;
import com.aionemu.chatserver.model.Race;

/**
 * @author ATracer
 */
public class RegionChannel extends RaceChannel {

	public RegionChannel(Race race, String identifier) {
		super(ChannelType.PUBLIC, race, identifier);
	}
}
