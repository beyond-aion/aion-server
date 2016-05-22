package com.aionemu.chatserver.model;

import java.util.Map;

import javolution.util.FastMap;

/**
 * @author ATracer
 * @modified Neon
 */
public enum ChannelType {

	REGION("public"),
	TRADE("trade"),
	LFG("partyFind"),
	JOB("job"),
	LANG("User");

	private static final Map<String, ChannelType> channelByIdentifier = new FastMap<>();

	private final String identifier;

	static {
		for (ChannelType ct : values())
			channelByIdentifier.put(ct.getIdentifier(), ct);
	}

	private ChannelType(String identifier) {
		this.identifier = identifier;
	}

	public String getIdentifier() {
		return identifier;
	}

	public static ChannelType getByIdentifier(String identifier) {
		return channelByIdentifier.get(identifier);
	}
}