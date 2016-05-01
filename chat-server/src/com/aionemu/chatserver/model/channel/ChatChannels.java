package com.aionemu.chatserver.model.channel;

import java.util.Arrays;
import java.util.List;

import javolution.util.FastTable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.chatserver.configs.Config;
import com.aionemu.chatserver.model.Race;
import com.aionemu.chatserver.service.GameServerService;

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

	/**
	 * LFG Channel
	 */
	static {
		addGroupChannel();
	}

	/**
	 * @param Channel the Channel to add.
	 */
	private static Channel addChannel(Channel Channel) {
		if (channels.add(Channel)) {
			return Channel;
		}
		return null;
	}

	/**
	 * @param channelId the channelId of the requesting Channel
	 * @return Channel with this channelId or throws an IllegalArgumentException if no channel with this id exists.
	 */
	public static Channel getChannelById(int channelId) {
		synchronized (channels) {
			for (Channel channel : channels) {
				if (channel.getChannelId() == channelId)
					return channel;
			}
		}
		if (Config.LOG_CHANNEL_INVALID) {
			log.warn("No registered channel with id " + channelId);
		}
		throw new IllegalArgumentException("no channel provided for id " + channelId);
	}

	/**
	 *
	 * @param identifier the byte identifier of the requested channel
	 * @param name the name of the requested channel
	 * @return Channel with this identifier or creates and returns a new channel if no channel with such identifier exists.<br>
	 * Null if no channel with this identifier exists and a new channel was not created
	 */
	public static Channel getChannelByIdentifierOrCreateNew(byte[] identifier, String name) {
		synchronized (channels) {
			for (Channel channel : channels) {
				if (Arrays.equals(channel.getIdentifierBytes(), identifier))
					return channel;
			}
		}
		Channel channel = null;
		if (!name.isEmpty()) {
			Race race = Race.ELYOS;
			if (name.endsWith(".1.AION.KOR")) {
				race = Race.ASMODIANS;
			}
			if (name.contains("public")) {
				channel = addChannel(new RegionChannel(race, name));
			} else if (name.contains("job")) {
				channel = addChannel(new JobChannel(race, name));
			} else if (name.contains("trade")) {
				channel = addChannel(new TradeChannel(race, name));
			} else if (name.contains("User")) {
				channel = addChannel(new LangChannel(race, name));
			}
		}
		return channel;
	}

	private static void addGroupChannel() {
		addChannel(new LfgChannel(Race.ELYOS, "@\u0001partyFind_PF\u0001" + GameServerService.GAMESERVER_ID + ".0.AION.KOR"));
		addChannel(new LfgChannel(Race.ASMODIANS, "@\u0001partyFind_PF\u0001" + GameServerService.GAMESERVER_ID + ".1.AION.KOR"));
	}
}