package com.aionemu.chatserver.model.channel;

import java.nio.charset.Charset;

import com.aionemu.chatserver.model.ChannelType;
import com.aionemu.chatserver.utils.IdFactory;

/**
 * @author ATracer
 */
public abstract class Channel {

	private final ChannelType channelType;
	private final byte[] identifierBytes;
	private final String identifier;
	private final int channelId;

	public Channel(ChannelType channelType, String identifier) {
		this.channelType = channelType;
		this.identifier = identifier;
		this.channelId = IdFactory.getInstance().nextId();
		this.identifierBytes = identifier.getBytes(Charset.forName("UTF-16le"));
	}

	public String getStringIdentifier() {
		return identifier;
	}

	public ChannelType getChannelType() {
		return channelType;
	}

	public byte[] getIdentifierBytes() {
		return identifierBytes;
	}

	public String getIdentifier() {
		return identifier;
	}

	public int getChannelId() {
		return channelId;
	}
}
