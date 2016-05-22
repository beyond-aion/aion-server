package com.aionemu.chatserver.service;

import java.util.HashMap;
import java.util.Map;

import com.aionemu.chatserver.model.ChatClient;
import com.aionemu.chatserver.model.message.Message;
import com.aionemu.chatserver.network.aion.serverpackets.SM_CHANNEL_MESSAGE;
import com.aionemu.chatserver.network.netty.handler.ClientChannelHandler;

/**
 * @author ATracer
 */
public class BroadcastService {

	private static BroadcastService instance = new BroadcastService();
	private Map<Integer, ChatClient> clients = new HashMap<Integer, ChatClient>();

	public static BroadcastService getInstance() {
		return instance;
	}

	private BroadcastService() {
	}

	/**
	 * @param client
	 */
	public void addClient(ChatClient client) {
		clients.put(client.getClientId(), client);
	}

	/**
	 * @param client
	 */
	public void removeClient(ChatClient client) {
		clients.remove(client.getClientId());
	}

	/**
	 * @param message
	 */
	public void broadcastMessage(Message message) {
		for (ChatClient client : clients.values()) {
			if (client.isInChannel(message.getChannel()))
				sendMessage(client, message);
		}
	}

	/**
	 * @param chatClient
	 * @param message
	 */
	public void sendMessage(ChatClient chatClient, Message message) {
		ClientChannelHandler cch = chatClient.getChannelHandler();
		cch.sendPacket(new SM_CHANNEL_MESSAGE(message));
	}
}
