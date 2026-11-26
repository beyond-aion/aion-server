package com.aionemu.chatserver.model.channel;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.chatserver.configs.main.LoggingConfig;
import com.aionemu.chatserver.model.ChannelType;
import com.aionemu.chatserver.model.ChatClient;
import com.aionemu.chatserver.model.Race;
import com.aionemu.chatserver.service.GameServerService;

/**
 * @author ATracer
 */
public class ChatChannels {

	/**
	 * A channel name is a map, class or language name, so 64 characters is generous.
	 */
	private static final int MAX_META_LENGTH = 64;
	/**
	 * Channels without members are removed, and a real server needs a few dozen at once.
	 */
	private static final int MAX_CHANNELS = 256;
	/**
	 * Guards channel creation and removal together with membership, so a channel can't be removed while someone is joining it.
	 */
	private static final Object membershipLock = new Object();

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
	 * Adds the client to the requested channel, creating the channel if needed. Channels the client leaves this way (the old region
	 * channel on map change) are removed once they have no members.
	 * 
	 * @return The joined channel or null if no channel with this identifier exists and a new channel was not created.
	 * @see #getOrCreate(ChatClient, String)
	 */
	public static Channel join(ChatClient client, String identifier) {
		synchronized (membershipLock) {
			Channel channel = getOrCreate(client, identifier);
			if (channel != null && !client.isInChannel(channel)) {
				channel.memberCount++;
				release(client.addChannel(channel));
			}
			return channel;
		}
	}

	/**
	 * @return True if the client was in this channel.
	 */
	public static boolean leave(ChatClient client, Channel channel) {
		if (channel == null)
			return false;
		synchronized (membershipLock) {
			if (!client.removeChannel(channel))
				return false;
			release(List.of(channel));
			return true;
		}
	}

	public static void leaveAll(ChatClient client) {
		synchronized (membershipLock) {
			release(client.removeAllChannels());
		}
	}

	private static void release(List<Channel> leftChannels) {
		for (Channel channel : leftChannels) {
			if (--channel.memberCount <= 0 && channels.remove(channel.getChannelId()) != null && LoggingConfig.LOG_CHANNEL_REQUEST)
				log.info("Removed empty channel {} (id: {})", channel.name(), channel.getChannelId());
		}
	}

	/**
	 * @param identifier
	 *          - the identifier of the requested channel, e.g. @trade_Housing_barrack1.0.AION.KOR
	 * @return Channel with this identifier or creates and returns a new channel if no channel with such identifier exists.<br>
	 *         Null if no channel with this identifier exists and a new channel was not created.
	 */
	private static Channel getOrCreate(ChatClient client, String identifier) {
		if (LoggingConfig.LOG_CHANNEL_REQUEST)
			log.info("{} requested channel: {}", client, identifier);

		String[] parts = identifier.split("\u0001"); // { @, trade_Housing_barrack, 1.0.AION.KOR }
		if (parts.length != 3)
			return null;

		String[] channelType = parts[1].split("_", 2); // { trade, Housing_barrack }
		if (channelType.length != 2) {
			log.warn("{} requested a channel without name: {}", client, identifier);
			return null;
		}

		ChannelType ct = ChannelType.getByIdentifier(channelType[0]);
		if (ct == null) {
			log.warn("{} requested a channel of an unknown type: {}", client, identifier);
			return null;
		}
		String channelMeta = channelType[1];
		if (channelMeta.length() > MAX_META_LENGTH) {
			log.warn("{} requested a channel with a too long name: {}", client, identifier);
			return null;
		}

		String[] channelRestrictions = parts[2].split("\\."); // { 1, 0, AION, KOR }
		if (channelRestrictions.length < 2) {
			log.warn("{} requested a channel with malformed restrictions: {}", client, parts[2]);
			return null;
		}
		int gameServerId;
		Race race;
		try {
			gameServerId = Integer.parseInt(channelRestrictions[0]);
			race = Race.getById(Integer.parseInt(channelRestrictions[1]));
		} catch (NumberFormatException e) {
			log.warn("{} requested a channel with malformed restrictions: {}", client, parts[2]);
			return null;
		}
		if (gameServerId == 99 || gameServerId != GameServerService.GAMESERVER_ID) { // retail: 99 is forbidden and it must be this session's server
			log.warn("{} requested a channel of game server {}: {}", client, gameServerId, identifier);
			return null;
		}
		if (race == null) {
			log.warn("{} requested a channel of an unknown race: {}", client, identifier);
			return null;
		}
		if (client.getRace() != race && client.getAccessLevel() == 0) {
			log.warn("{} requested channel of race: {}", client, race);
			return null;
		}

		for (Channel channel : channels.values()) {
			if (channel.matches(ct, gameServerId, race, channelMeta))
				return channel;
		}
		if (channels.size() >= MAX_CHANNELS) {
			log.warn("{} requested a new channel, but {} already exist: {}", client, channels.size(), identifier);
			return null;
		}
		Channel channel = addChannel(ct, gameServerId, race, channelMeta);
		if (channel instanceof JobChannel jobChannel && !jobChannel.hasAliases()) {
			log.warn("{} requested channel for unknown class: {}", client, channelMeta);
		}
		return channel;
	}
}
