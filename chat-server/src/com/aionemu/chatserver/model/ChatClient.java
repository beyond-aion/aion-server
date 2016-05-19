package com.aionemu.chatserver.model;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.chatserver.model.channel.Channel;
import com.aionemu.chatserver.network.netty.handler.ClientChannelHandler;

import javolution.util.FastMap;

/**
 * @author ATracer
 */
public class ChatClient {

	private final Logger log = LoggerFactory.getLogger(ChatClient.class);

	/**
	 * Id of chat client (player id)
	 */
	private int clientId;

	/**
	 * Identifier used when sending message
	 */
	private byte[] identifier;

	/**
	 * Token used during auth with GS
	 */
	private byte[] token;

	/**
	 * Channel handler of chat client
	 */
	private ClientChannelHandler channelHandler;

	/**
	 * Map with all connected channels<br>
	 * Only one channel of specific type can be added
	 */
	private Map<ChannelType, Channel> channelsList = new FastMap<>();
	private Map<ChannelType, Long> lastMessageTime = new FastMap<>();
	private String realName;
	private long gagTime;

	/**
	 * @param clientId
	 * @param token
	 * @param playerLogin
	 * @param nick
	 * @param identifier
	 */
	public ChatClient(int clientId, byte[] token, String nick) {
		this.clientId = clientId;
		this.token = token;
		this.realName = nick;
	}

	/**
	 * @param channel
	 */
	public void addChannel(Channel channel) {
		channelsList.put(channel.getChannelType(), channel);
	}

	/**
	 * @return the channelHandler
	 */
	public ClientChannelHandler getChannelHandler() {
		return channelHandler;
	}

	/**
	 * @return the clientId
	 */
	public int getClientId() {
		return clientId;
	}

	/**
	 * @return the identifier
	 */
	public byte[] getIdentifier() {
		return identifier;
	}

	public long getLastMessageTime(ChannelType ct) {
		return lastMessageTime.getOrDefault(ct, 0L);
	}

	public void updateLastMessageTime(ChannelType ct) {
		lastMessageTime.put(ct, System.currentTimeMillis());
	}

	public String getRealName() {
		return realName;
	}

	/**
	 * @return the token
	 */
	public byte[] getToken() {
		return token;
	}

	/**
	 * @param channel
	 */
	public boolean isInChannel(Channel channel) {
		return channelsList.containsKey(channel.getChannelType());
	}

	/**
	 * @param channelHandler
	 *          the channelHandler to set
	 */
	public void setChannelHandler(ClientChannelHandler channelHandler) {
		this.channelHandler = channelHandler;
	}

	/**
	 * @param identifier
	 *          the identifier to set
	 * @param realAccount
	 * @param realName
	 */
	public void setIdentifier(byte[] identifier) {
		this.identifier = identifier;
	}

	/**
	 * @param ct
	 * @return The protection time (delay) in seconds, when the client can chat in the specified channel again.
	 */
	public int getFloodProtectionTime(ChannelType ct) {
		int delay = ct == ChannelType.LFG || ct == ChannelType.TRADE ? 30000 : 1000; // implemented same as on client-side
		long floodProtectionTime = delay - (System.currentTimeMillis() - getLastMessageTime(ct));

		if (floodProtectionTime > 0) {
			return Math.max(1, (int) (floodProtectionTime / 1000));
		}
		return 0;
	}

	public boolean isGagged() {
		if (gagTime == 0)
			return false;
		return System.currentTimeMillis() < gagTime;
	}

	public void setGagTime(long gagTime) {
		this.gagTime = gagTime;
	}

	public long getGagTime() {
		return this.gagTime;
	}

	public boolean same(String nick) {
		if (!this.realName.equals(nick)) {
			log.warn("chat hack! different name " + nick + ". expected " + this.realName);
			return true;
		}
		return true;
	}
}
