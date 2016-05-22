package com.aionemu.chatserver.model.channel;

import com.aionemu.chatserver.model.ChannelType;
import com.aionemu.chatserver.model.Race;

/**
 * @modified Neon
 */
public class LangChannel extends RaceChannel {

	public LangChannel(int gameServerId, Race race) {
		super(ChannelType.LANG, gameServerId, race);
	}
}
