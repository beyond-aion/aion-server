package com.aionemu.chatserver.model.channel;

import com.aionemu.chatserver.model.ChannelType;
import com.aionemu.chatserver.model.Race;
import com.aionemu.chatserver.utils.IdFactory;

/**
 * @author ATracer, Neon
 */
public abstract class Channel {

	private final ChannelType channelType;
	private final int gameServerId;
	private final int channelId;

	public Channel(ChannelType channelType, int gameServerId) {
		this.channelType = channelType;
		this.gameServerId = gameServerId;
		this.channelId = IdFactory.getInstance().nextId();
	}

	public ChannelType getChannelType() {
		return channelType;
	}

	public int getGameServerId() {
		return gameServerId;
	}

	/**
	 * @return The unique id of this channel.
	 */
	public int getChannelId() {
		return channelId;
	}

	/**
	 * @return True, if the channel matches the specified criteria. Used to determine if a clients request matches an existing channel or we need to
	 *         create a new one.
	 */
	public abstract boolean matches(ChannelType channelType, int gameServerId, Race race, String channelMeta);

	/**
	 * @return The name of this channel (mainly for logging purposes).
	 */
	public abstract String name();
}
