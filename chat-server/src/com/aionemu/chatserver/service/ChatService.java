package com.aionemu.chatserver.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.chatserver.model.ChatClient;
import com.aionemu.chatserver.model.Race;
import com.aionemu.chatserver.model.channel.Channel;
import com.aionemu.chatserver.model.channel.ChatChannels;
import com.aionemu.chatserver.network.aion.AionConnection;
import com.aionemu.chatserver.network.aion.AionConnection.State;
import com.aionemu.chatserver.network.aion.serverpackets.SM_CHANNEL_RESPONSE;
import com.aionemu.chatserver.network.aion.serverpackets.SM_PLAYER_AUTH_RESPONSE;
import com.aionemu.commons.utils.Rnd;

/**
 * @author ATracer
 */
public class ChatService {

	private static final Logger log = LoggerFactory.getLogger(ChatService.class);
	private static final ChatService instance = new ChatService();
	private final Map<Integer, ChatClient> players = new ConcurrentHashMap<>();

	private ChatService() {

	}

	public static ChatService getInstance() {
		return instance;
	}

	public ChatClient registerPlayer(int playerId, String accName, String nick, Race race, byte accessLevel) throws NoSuchAlgorithmException {
		MessageDigest md = MessageDigest.getInstance("SHA-256");
		md.reset();
		md.update(accName.getBytes(StandardCharsets.UTF_8), 0, accName.length());
		byte[] accountToken = md.digest();
		byte[] token = generateToken(accountToken);
		ChatClient chatClient = new ChatClient(playerId, token, accName, nick, race, accessLevel);
		players.put(playerId, chatClient);
		return chatClient;
	}

	private byte[] generateToken(byte[] accountToken) {
		byte[] dynamicToken = new byte[16];
		Rnd.nextBytes(dynamicToken);
		byte[] token = new byte[48];
		for (int i = 0; i < token.length; i++) {
			if (i < 16)
				token[i] = dynamicToken[i];
			else
				token[i] = accountToken[i - 16];
		}
		return token;
	}

	public void registerPlayerConnection(int playerId, byte[] token, byte[] identifier, String name, String accName,
		AionConnection connection) {
		ChatClient chatClient = players.get(playerId);
		if (chatClient == null)
			log.warn("Client tried to connect but was not yet registered from game server side");
		else if (!Arrays.equals(chatClient.getToken(), token))
			log.warn("Client tried to connect but given token doesn't match");
		else if (!chatClient.getAccountName().equalsIgnoreCase(accName)) // client sends accName lowercase
			log.warn("Client tried to connect with account name: {} (expected: {})", accName, chatClient.getAccountName());
		else if (!chatClient.getName().equals(name))
			log.warn("Client tried to connect with character name: {} (expected: {})", name, chatClient.getName());
		else {
			chatClient.setIdentifier(identifier);
			chatClient.setConnection(connection);
			connection.sendPacket(new SM_PLAYER_AUTH_RESPONSE());
			connection.setState(State.AUTHED);
			connection.setChatClient(chatClient);
			BroadcastService.getInstance().addClient(chatClient);
		}
	}

	public void registerPlayerWithChannel(AionConnection connection, int channelRequestId, String identifier) {
		Channel channel = ChatChannels.getOrCreate(connection.getChatClient(), identifier);
		if (channel != null) {
			connection.getChatClient().addChannel(channel);
			connection.sendPacket(new SM_CHANNEL_RESPONSE(channel, channelRequestId));
		}
	}

	public void playerLogout(int playerId) {
		ChatClient chatClient = players.remove(playerId);
		if (chatClient != null) {
			BroadcastService.getInstance().removeClient(chatClient);
			log.info("Player[id={}] logged out ", playerId);
			if (chatClient.getConnection() != null)
				chatClient.getConnection().close();
			else
				log.warn("Received logout event without client authentication for player {}", playerId);
		}
	}

	public void gagPlayer(int playerId, long gagTimeMillis) {
		ChatClient client = players.get(playerId);
		if (client != null) {
			client.setGagTime(gagTimeMillis);
			log.info("Player[id={}] was gagged for {} minutes", playerId, gagTimeMillis / 60000);
		}
	}
}
