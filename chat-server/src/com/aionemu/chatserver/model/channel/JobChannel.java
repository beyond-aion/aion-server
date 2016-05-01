package com.aionemu.chatserver.model.channel;

import com.aionemu.chatserver.model.ChannelType;
import com.aionemu.chatserver.model.Race;

/**
 * @author ATracer
 */
public class JobChannel extends RaceChannel {

	public JobChannel(Race race, String identifier) {
		super(ChannelType.JOB, race, identifier);
	}
}
