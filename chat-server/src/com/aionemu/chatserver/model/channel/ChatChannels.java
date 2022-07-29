package com.aionemu.chatserver.model.channel;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.chatserver.configs.main.LoggingConfig;
import com.aionemu.chatserver.model.ChannelType;
import com.aionemu.chatserver.model.ChatClient;
import com.aionemu.chatserver.model.Race;

/**
 * @author ATracer
 */
public class ChatChannels {

	private static final Logger log = LoggerFactory.getLogger(ChatChannels.class);
	private static final Map<Integer, Channel> channels = new ConcurrentHashMap<>();

	private static Channel addChannel(ChannelType ct, int gameServerId, Race race, String channelMeta) {
		Channel channel = switch (ct) {
			case REGION -> new RegionChannel(gameServerId, race, channelMeta);
			case TRADE -> new TradeChannel(gameServerId, race, channelMeta);
			case LFG -> new LfgChannel(gameServerId, race);
			case JOB -> new JobChannel(gameServerId, race, channelMeta);
			case LANG -> new LangChannel(gameServerId, race, channelMeta);
		};
		channels.put(channel.getChannelId(), channel);
		return channel;
	}

	/**
	 * @param channelId
	 *          the channelId of the requesting Channel
	 * @return Channel with this channelId or null if no channel with this id exists.
	 */
	public static Channel getChannelById(int channelId) {
		Channel channel = channels.get(channelId);
		if (channel == null && LoggingConfig.LOG_CHANNEL_INVALID)
			log.warn("No registered channel with id {}", channelId);
		return channel;
	}

	/**
	 * @param identifier
	 *          - the identifier of the requested channel, e.g. @trade_Housing_barrack1.0.AION.KOR
	 * @return Channel with this identifier or creates and returns a new channel if no channel with such identifier exists.<br>
	 *         Null if no channel with this identifier exists and a new channel was not created.
	 */
	public static Channel getOrCreate(ChatClient client, String identifier) {
		if (LoggingConfig.LOG_CHANNEL_REQUEST)
			log.info("{} requested channel: {}", client, identifier);

		String[] parts = identifier.split("\u0001"); // { @, trade_Housing_barrack, 1.0.AION.KOR }
		if (parts.length != 3)
			return null;

		String[] channelType = parts[1].split("_", 2); // { trade, Housing_barrack }
		String[] channelRestrictions = parts[2].split("\\."); // { 1, 0, AION, KOR }

		ChannelType ct = ChannelType.getByIdentifier(channelType[0]);
		String channelMeta = channelType[1];
		int gameServerId = Integer.parseInt(channelRestrictions[0]);
		Race race = Race.getById(Integer.parseInt(channelRestrictions[1]));
		if (client.getRace() != race && client.getAccessLevel() == 0) {
			log.warn("{} requested channel of race: {}", client, race);
			return null;
		}

		for (Channel channel : channels.values()) {
			if (channel.matches(ct, gameServerId, race, channelMeta))
				return channel;
		}
		Channel channel = addChannel(ct, gameServerId, race, channelMeta);
		if (channel instanceof JobChannel jobChannel && !jobChannel.hasAliases()) {
			log.warn("{} requested channel for unknown class: {}", client, channelMeta);
		}
		return channel;
	}
}
