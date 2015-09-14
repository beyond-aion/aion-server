package com.aionemu.chatserver.model;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.chatserver.configs.Config;
import com.aionemu.chatserver.model.channel.Channel;
import com.aionemu.chatserver.network.netty.handler.ClientChannelHandler;

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
	private Map<ChannelType, Channel> channelsList = new HashMap<ChannelType, Channel>();

	private long lastMessage;

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

	public boolean verifyLastMessage() {
		if (Config.MESSAGE_DELAY == 0)
			return true;

		if (this.lastMessage == 0) {
			this.lastMessage = System.currentTimeMillis();
			return true;
		} else {
			long diff = System.currentTimeMillis() - this.lastMessage;
			if (Config.MESSAGE_DELAY * 1000 > diff) {
				log.warn("player " + this.getClientId() + " tried to flood (" + diff + "ms) traffic. skipped");
				return false;
			} else {
				this.lastMessage = System.currentTimeMillis();
				return true;
			}
		}
	}

	public boolean isGagged() {
		if (this.gagTime == 0)
			return false;
		if (System.currentTimeMillis() > this.gagTime)
			return false;
		return true;
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
