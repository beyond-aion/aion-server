package com.aionemu.chatserver.model.channel;

import com.aionemu.chatserver.model.ChannelType;
import com.aionemu.chatserver.model.Race;

/**
 * @author Unknown, Neon
 */
public class LangChannel extends RaceChannel {

	private final String language;

	public LangChannel(int gameServerId, Race race, String language) {
		super(ChannelType.LANG, gameServerId, race);
		this.language = language;
	}

	public String getLanguage() {
		return language;
	}

	@Override
	public boolean matches(ChannelType channelType, int gameServerId, Race race, String language) {
		return super.matches(channelType, gameServerId, race, language) && getLanguage().equals(language);
	}

	@Override
	public String name() {
		return getChannelType().name() + ": " + language + " (" + getRace().name().charAt(0) + ")";
	}
}
