package com.aionemu.chatserver.model.channel;

import com.aionemu.chatserver.model.ChannelType;
import com.aionemu.chatserver.model.Race;

/**
 * @author ATracer
 */
public class TradeChannel extends RaceChannel {

	public TradeChannel(Race race, String identifier) {
		super(ChannelType.TRADE, race, identifier);
	}
}
