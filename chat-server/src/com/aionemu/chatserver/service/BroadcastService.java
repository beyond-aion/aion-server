package com.aionemu.chatserver.service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.aionemu.chatserver.model.ChatClient;
import com.aionemu.chatserver.model.message.Message;
import com.aionemu.chatserver.network.aion.serverpackets.SM_CHANNEL_MESSAGE;

/**
 * @author ATracer
 */
public class BroadcastService {

	private static final BroadcastService instance = new BroadcastService();
	private final Map<Integer, ChatClient> clients = new ConcurrentHashMap<>();

	public static BroadcastService getInstance() {
		return instance;
	}

	private BroadcastService() {

	}

	public void addClient(ChatClient client) {
		clients.put(client.getClientId(), client);
	}

	public void removeClient(ChatClient client) {
		clients.remove(client.getClientId());
	}

	public void broadcastMessage(Message message) {
		for (ChatClient client : clients.values()) {
			if (client.isInChannel(message.getChannel()))
				sendMessage(client, message);
		}
	}

	public void sendMessage(ChatClient chatClient, Message message) {
		chatClient.getChannelHandler().sendPacket(new SM_CHANNEL_MESSAGE(message));
	}
}
