package com.aionemu.chatserver.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
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

/**
 * @author ATracer
 */
public class ChatService {

	private static final Logger log = LoggerFactory.getLogger(ChatService.class);
	private static final ChatService instance = new ChatService();
	private static final SecureRandom secureRandom = new SecureRandom();
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
		ChatClient previous = players.put(playerId, chatClient);
		if (previous != null) {
			AionConnection previousConnection;
			synchronized (previous) {
				BroadcastService.getInstance().removeClient(previous);
				previousConnection = previous.getConnection();
			}
			if (previousConnection != null)
				previousConnection.close();
		}
		return chatClient;
	}

	private byte[] generateToken(byte[] accountToken) {
		byte[] dynamicToken = new byte[16];
		secureRandom.nextBytes(dynamicToken);
		byte[] token = new byte[48];
		for (int i = 0; i < token.length; i++) {
			if (i < 16)
				token[i] = dynamicToken[i];
			else
				token[i] = accountToken[i - 16];
		}
		return token;
	}

	public void registerPlayerConnection(int msgIndex, int playerId, byte[] token, byte[] identifier, String accName, AionConnection connection) {
		String nameIdentifier = new String(identifier, StandardCharsets.UTF_16LE); // Name@identifier
		int endIndex = nameIdentifier.lastIndexOf('@');
		String name = endIndex == -1 ? nameIdentifier : nameIdentifier.substring(0, endIndex);
		ChatClient chatClient = players.get(playerId);
		if (chatClient == null)
			log.warn("Client tried to connect but was not yet registered from game server side with player ID {} (name ID: {})", playerId, nameIdentifier);
		else if (!Arrays.equals(chatClient.getToken(), token))
			log.warn("Client tried to connect but given token doesn't match (account: {})", chatClient.getAccountName());
		else if (!chatClient.getAccountName().equalsIgnoreCase(accName)) // client sends accName lowercase
			log.warn("Client tried to connect with account name: {} (expected: {})", accName, chatClient.getAccountName());
		else if (!chatClient.getName().equals(name))
			log.warn("Client tried to connect with character name: {} (expected: {})", name, chatClient.getName());
		else {
			AionConnection previousConnection;
			synchronized (chatClient) { // a connection of the character that closes right now must not take it out of the broadcasts afterwards
				if (players.get(playerId) != chatClient) { // logged out since it was looked up, see playerLogout
					connection.close();
					return;
				}
				previousConnection = chatClient.getConnection();
				chatClient.setIdentifier(identifier);
				chatClient.setConnection(connection);
				connection.sendPacket(new SM_PLAYER_AUTH_RESPONSE(msgIndex));
				connection.setState(State.AUTHED);
				connection.setChatClient(chatClient);
				BroadcastService.getInstance().addClient(chatClient);
			}
			// The character has one connection, like it has one in the game. The previous one stayed authenticated, and could keep writing to the
			// channels in the character's name.
			if (previousConnection != null && previousConnection != connection) {
				log.info("{} logged in again, closing its previous connection from {}", chatClient, previousConnection.getIP());
				previousConnection.close();
			}
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
			AionConnection connection;
			// a connection that authenticates right now either sees the character gone or is closed here
			synchronized (chatClient) {
				BroadcastService.getInstance().removeClient(chatClient);
				connection = chatClient.getConnection();
			}
			log.info("Player[id={}] logged out ", playerId);
			if (connection != null)
				connection.close();
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
