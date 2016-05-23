package com.aionemu.chatserver.model;

import java.util.List;
import java.util.Map;

import com.aionemu.chatserver.model.channel.Channel;
import com.aionemu.chatserver.network.netty.handler.ClientChannelHandler;

import javolution.util.FastMap;
import javolution.util.FastTable;

/**
 * @author ATracer
 * @modified Neon
 */
public class ChatClient {

	private final int clientId;
	private final byte[] token;
	private final String accName;
	private final String name;
	private final Race race;
	private byte[] identifier;
	private ClientChannelHandler channelHandler;
	private long gagTime;

	/**
	 * Map with all connected channels<br>
	 * Support for 2 channels of ChannelType.JOB, since starting classes join both main class channels
	 */
	private Map<ChannelType, List<Channel>> channels = new FastMap<>();
	private Map<ChannelType, Long> lastMessageTime = new FastMap<>();

	public ChatClient(int clientId, byte[] token, String accName, String nick, Race race) {
		this.clientId = clientId;
		this.token = token;
		this.accName = accName;
		this.name = nick;
		this.race = race;
	}

	/**
	 * @return Unique id of the chat client (player id)
	 */
	public int getClientId() {
		return clientId;
	}

	/**
	 * @return Token used during auth with GS
	 */
	public byte[] getToken() {
		return token;
	}

	public String getAccountName() {
		return accName;
	}

	public String getName() {
		return name;
	}

	public Race getRace() {
		return race;
	}

	/**
	 * @return Identifier used when sending messages
	 */
	public byte[] getIdentifier() {
		return identifier;
	}

	public void setIdentifier(byte[] identifier) {
		this.identifier = identifier;
	}

	public ClientChannelHandler getChannelHandler() {
		return channelHandler;
	}

	public void setChannelHandler(ClientChannelHandler channelHandler) {
		this.channelHandler = channelHandler;
	}

	public void addChannel(Channel channel) {
		List<Channel> channelsOfType = channels.get(channel.getChannelType());
		if (channelsOfType == null)
			channels.put(channel.getChannelType(), FastTable.of(channel));
		else {
			if (channel.getChannelType() != ChannelType.JOB || channelsOfType.size() == 2)
				channelsOfType.clear();
			channelsOfType.add(channel);
		}
	}

	public boolean isInChannel(Channel channel) {
		List<Channel> channelsOfType = channels.get(channel.getChannelType());
		return channelsOfType != null && channelsOfType.stream().anyMatch(ch -> ch.getChannelId() == channel.getChannelId());
	}

	public long getLastMessageTime(ChannelType ct) {
		return lastMessageTime.getOrDefault(ct, 0L);
	}

	public void updateLastMessageTime(ChannelType ct) {
		lastMessageTime.put(ct, System.currentTimeMillis());
	}

	/**
	 * @param ct
	 * @return The protection time (delay) in seconds, when the client can chat in the specified channel again.
	 */
	public int getFloodProtectionTime(ChannelType ct) {
		int delay = ct == ChannelType.LFG || ct == ChannelType.TRADE ? 30000 : 1000; // implemented same as on client-side
		long floodProtectionTime = delay - (System.currentTimeMillis() - getLastMessageTime(ct));

		return floodProtectionTime <= 0 ? 0 : Math.max(1, (int) (floodProtectionTime / 1000));
	}

	public boolean isGagged() {
		return gagTime > 0 && System.currentTimeMillis() < gagTime;
	}

	public void setGagTime(long gagTime) {
		this.gagTime = gagTime;
	}

	public long getGagTime() {
		return this.gagTime;
	}

	@Override
	public String toString() {
		return "Player [name=" + name + ", id=" + clientId + ", race=" + race + "]";
	}
}
