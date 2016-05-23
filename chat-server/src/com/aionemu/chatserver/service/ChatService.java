package com.aionemu.chatserver.service;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.chatserver.model.ChatClient;
import com.aionemu.chatserver.model.Race;
import com.aionemu.chatserver.model.channel.Channel;
import com.aionemu.chatserver.model.channel.ChatChannels;
import com.aionemu.chatserver.network.aion.serverpackets.SM_CHANNEL_RESPONSE;
import com.aionemu.chatserver.network.aion.serverpackets.SM_PLAYER_AUTH_RESPONSE;
import com.aionemu.chatserver.network.netty.handler.ClientChannelHandler;
import com.aionemu.chatserver.network.netty.handler.ClientChannelHandler.State;

/**
 * @author ATracer
 */
public class ChatService {

	private static ChatService instance = new ChatService();
	private static final Logger log = LoggerFactory.getLogger(ChatService.class);
	private Map<Integer, ChatClient> players = new HashMap<Integer, ChatClient>();

	public static ChatService getInstance() {
		return instance;
	}

	private ChatService() {
	}

	/**
	 * Player registered from server side
	 * 
	 * @param playerId
	 * @param nick
	 * @param token
	 * @param identifier
	 * @return
	 * @throws NoSuchAlgorithmException
	 */
	public ChatClient registerPlayer(int playerId, String accName, String nick, Race race) throws NoSuchAlgorithmException, UnsupportedEncodingException {
		MessageDigest md = MessageDigest.getInstance("SHA-256");
		md.reset();
		md.update(accName.getBytes("UTF-8"), 0, accName.length());
		byte[] accountToken = md.digest();
		byte[] token = generateToken(accountToken);
		ChatClient chatClient = new ChatClient(playerId, token, accName, nick, race);
		players.put(playerId, chatClient);
		return chatClient;
	}

	/**
	 * @param playerId
	 * @return
	 */
	private byte[] generateToken(byte[] accountToken) {
		byte[] dynamicToken = new byte[16];
		new Random().nextBytes(dynamicToken);
		byte[] token = new byte[48];
		for (int i = 0; i < token.length; i++) {
			if (i < 16)
				token[i] = dynamicToken[i];
			else
				token[i] = accountToken[i - 16];
		}
		return token;
	}

	/**
	 * Player registered from client request
	 * 
	 * @param playerId
	 * @param token
	 * @param identifier
	 * @param name
	 * @param accName
	 * @param clientChannelHandler
	 * @throws UnsupportedEncodingException
	 */
	public void registerPlayerConnection(int playerId, byte[] token, byte[] identifier, String name, String accName, ClientChannelHandler channelHandler)
		throws UnsupportedEncodingException {
		ChatClient chatClient = players.get(playerId);
		if (chatClient == null)
			log.warn("Client tried to connect but was not yet registered from game server side");
		else if (!Arrays.equals(chatClient.getToken(), token))
			log.warn("Client tried to connect but given token doesn't match");
		else if (!chatClient.getAccountName().equals(accName))
			log.warn("Client tried to connect with account name: " + accName + " (expected: " + chatClient.getAccountName() + ")");
		else if (!chatClient.getName().equals(name))
			log.warn("Client tried to connect with character name: " + name + " (expected: " + chatClient.getName() + ")");
		else {
			chatClient.setIdentifier(identifier);
			chatClient.setChannelHandler(channelHandler);
			channelHandler.sendPacket(new SM_PLAYER_AUTH_RESPONSE());
			channelHandler.setState(State.AUTHED);
			channelHandler.setChatClient(chatClient);
			BroadcastService.getInstance().addClient(chatClient);
		}
	}

	/**
	 * @param chatClient
	 * @param channelIndex
	 * @param channelIdentifier
	 * @return
	 */
	public void registerPlayerWithChannel(ClientChannelHandler clientChannelHandler, int channelRequestId, String name) {
		Channel channel = ChatChannels.getOrCreate(clientChannelHandler.getChatClient(), name);
		if (channel != null) {
			clientChannelHandler.getChatClient().addChannel(channel);
			clientChannelHandler.sendPacket(new SM_CHANNEL_RESPONSE(channel, channelRequestId));
		}
	}

	/**
	 * @param playerId
	 */
	public void playerLogout(int playerId) {
		ChatClient chatClient = players.get(playerId);
		if (chatClient != null) {
			players.remove(playerId);
			BroadcastService.getInstance().removeClient(chatClient);
			if (chatClient.getChannelHandler() != null)
				chatClient.getChannelHandler().close();
			else
				log.warn("Received logout event without client authentication for player " + playerId);
		}
	}

	/**
	 * @param playerId
	 * @param gagTime
	 */
	public void gagPlayer(int playerId, long gagTime) {
		if (players.containsKey(playerId)) {
			ChatClient client = players.get(playerId);
			client.setGagTime(gagTime);
		}
	}
}
