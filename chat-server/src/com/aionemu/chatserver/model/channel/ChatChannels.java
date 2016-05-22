package com.aionemu.chatserver.model.channel;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.chatserver.configs.main.LoggingConfig;
import com.aionemu.chatserver.model.ChannelType;
import com.aionemu.chatserver.model.PlayerClass;
import com.aionemu.chatserver.model.Race;

import javolution.util.FastTable;

/**
 * @author ATracer
 */
public class ChatChannels {

	/**
	 * Logger
	 */
	private static final Logger log = LoggerFactory.getLogger(ChatChannels.class);

	/**
	 * Channel List
	 */
	private static final List<Channel> channels = new FastTable<>();

	private static Channel addChannel(ChannelType ct, String channelMeta, int gameServerId, Race race) {
		Channel channel = null;
		switch (ct) {
			case REGION:
				channel = new RegionChannel(gameServerId, race, channelMeta);
				break;
			case TRADE:
				channel = new TradeChannel(gameServerId, race, channelMeta);
				break;
			case LFG:
				channel = new LfgChannel(gameServerId, race);
				break;
			case JOB:
				PlayerClass playerClass = PlayerClass.getClassByIdentifier(channelMeta);
				if (playerClass != null)
					channel = new JobChannel(gameServerId, race, playerClass);
				else
					log.warn("Client requested non existent class channel: " + channelMeta);
				break;
			case LANG:
				channel = new LangChannel(gameServerId, race);
				break;
		}
		if (channel != null)
			channels.add(channel);
		return channel;
	}

	/**
	 * @param channelId
	 *          the channelId of the requesting Channel
	 * @return Channel with this channelId or throws an IllegalArgumentException if no channel with this id exists.
	 */
	public static Channel getChannelById(int channelId) {
		synchronized (channels) {
			for (Channel channel : channels) {
				if (channel.getChannelId() == channelId)
					return channel;
			}
		}
		if (LoggingConfig.LOG_CHANNEL_INVALID) {
			log.warn("No registered channel with id " + channelId);
		}
		throw new IllegalArgumentException("no channel provided for id " + channelId);
	}

	/**
	 * @param name
	 *          - the name of the requested channel
	 * @return Channel with this identifier or creates and returns a new channel if no channel with such identifier exists.<br>
	 *         Null if no channel with this identifier exists and a new channel was not created
	 */
	public static Channel getOrCreate(String name) {
		String[] parts = name.split("\u0001"); // @trade_Housing_barrack1.0.AION.KOR
		if (parts.length != 3)
			return null;
		String[] channelType = parts[1].split("_", 2); // { trade, Housing_barrack }
		String[] channelRestrictions = parts[2].split("\\."); // { 1, 0, AION, KOR }

		ChannelType ct = ChannelType.getByIdentifier(channelType[0]);
		String channelMeta = channelType[1];
		int gameServerId = Integer.valueOf(channelRestrictions[0]);
		Race race = Race.getById(Integer.valueOf(channelRestrictions[1]));
		synchronized (channels) {
			for (Channel channel : channels) {
				if (channel.matches(ct, gameServerId, race, channelMeta))
					return channel;
			}
		}
		return addChannel(ct, channelMeta, gameServerId, race);
	}
}
