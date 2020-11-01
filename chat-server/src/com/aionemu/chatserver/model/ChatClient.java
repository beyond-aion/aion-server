package com.aionemu.chatserver.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.aionemu.chatserver.model.channel.Channel;
import com.aionemu.chatserver.network.netty.handler.ClientChannelHandler;

/**
 * @author ATracer, Neon
 */
public class ChatClient {

	private final int clientId;
	private final byte[] token;
	private final String accName;
	private final String name;
	private final Race race;
	private final byte accessLevel;
	private byte[] identifier;
	private ClientChannelHandler channelHandler;
	private long gagTime;

	/**
	 * Map with all connected channels<br>
	 * Support for 2 channels of ChannelType.JOB, since starting classes join both main class channels
	 */
	private final Map<ChannelType, List<Channel>> channels = new ConcurrentHashMap<>();
	private final Map<ChannelType, Long> lastMessageTime = new ConcurrentHashMap<>();

	public ChatClient(int clientId, byte[] token, String accName, String nick, Race race, byte accessLevel) {
		this.clientId = clientId;
		this.token = token;
		this.accName = accName;
		this.name = nick;
		this.race = race;
		this.accessLevel = accessLevel;
	}

	public int getClientId() {
		return clientId;
	}

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

	public byte getAccessLevel() {
		return accessLevel;
	}

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
		if (channelsOfType == null) {
			channelsOfType = new ArrayList<>();
			channels.put(channel.getChannelType(), channelsOfType);
		} else if (channel.getChannelType() != ChannelType.JOB || channelsOfType.size() == 2) {
			channelsOfType.clear();
		}
		channelsOfType.add(channel);
	}

	public boolean removeChannel(Channel channel) {
		if (channel != null) {
			List<Channel> channelsOfType = channels.get(channel.getChannelType());
			if (channelsOfType != null)
				return channelsOfType.removeIf(ch -> ch.getChannelId() == channel.getChannelId());
		}
		return false;
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

	public int nextMessageTimeSec(ChannelType ct) {
		int delay = ct == ChannelType.LFG || ct == ChannelType.TRADE ? 30000 : 1000; // implemented same as on client-side
		long floodProtectionTime = delay - (System.currentTimeMillis() - getLastMessageTime(ct));
		return floodProtectionTime <= 0 ? 0 : Math.max(1, (int) (floodProtectionTime / 1000));
	}

	public boolean isGagged() {
		return gagTime > 0 && System.currentTimeMillis() < gagTime;
	}

	public long getGagTime() {
		return this.gagTime;
	}

	public void setGagTime(long gagTime) {
		this.gagTime = gagTime;
	}

	@Override
	public String toString() {
		return "Player [name=" + name + ", id=" + clientId + ", race=" + race + "]";
	}
}
