package com.aionemu.chatserver.model;

import java.util.HashMap;
import java.util.Map;

/**
 * @author ATracer, Neon
 */
public enum ChannelType {

	REGION("public"),
	TRADE("trade"),
	LFG("partyFind"),
	JOB("job"),
	LANG("User");

	private static final Map<String, ChannelType> channelByIdentifier = new HashMap<>();

	static {
		for (ChannelType ct : values())
			channelByIdentifier.put(ct.getIdentifier(), ct);
	}

	private final String identifier;

	ChannelType(String identifier) {
		this.identifier = identifier;
	}

	public static ChannelType getByIdentifier(String identifier) {
		return channelByIdentifier.get(identifier);
	}

	public String getIdentifier() {
		return identifier;
	}
}
