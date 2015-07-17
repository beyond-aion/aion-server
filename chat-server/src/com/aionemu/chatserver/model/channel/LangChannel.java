package com.aionemu.chatserver.model.channel;

import com.aionemu.chatserver.model.ChannelType;
import com.aionemu.chatserver.model.Race;

public class LangChannel extends RaceChannel
{
	public LangChannel(Race race, String identifier)
	{
		super(ChannelType.LANG, race, identifier);
	}
}
