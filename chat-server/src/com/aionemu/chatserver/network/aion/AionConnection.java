package com.aionemu.chatserver.network.aion;

import java.nio.ByteBuffer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.chatserver.configs.network.NetworkConfig;
import com.aionemu.chatserver.model.ChatClient;
import com.aionemu.chatserver.network.netty.NettyConnection;
import com.aionemu.commons.network.packet.BaseClientPacket;

import io.netty.channel.Channel;
import io.netty.util.concurrent.EventExecutor;

/**
 * Connection of an Aion game client to the chat server.
 */
public class AionConnection extends NettyConnection<AbstractServerPacket> {

	private static final Logger log = LoggerFactory.getLogger(AionConnection.class);

	private volatile State state = State.CONNECTED;
	private volatile ChatClient chatClient;

	public enum State {
		CONNECTED,
		AUTHED
	}

	public AionConnection(Channel channel, EventExecutor packetExecutor) {
		super(channel, packetExecutor);
	}

	@Override
	protected BaseClientPacket<?> createPacket(ByteBuffer data) {
		return ClientPacketHandler.handle(data, this);
	}

	@Override
	protected void writePacket(AbstractServerPacket packet, ByteBuffer buffer) {
		packet.write(this, buffer);
	}

	@Override
	protected boolean isAuthenticated() {
		return state == State.AUTHED;
	}

	@Override
	protected long getAuthTimeoutMillis() {
		return NetworkConfig.CLIENT_AUTH_TIMEOUT_SECONDS * 1000L;
	}

	@Override
	protected int getMaxPendingPackets() {
		return 100;
	}

	@Override
	protected long getMaxPendingWriteBytes() {
		return 256 * 1024;
	}

	@Override
	protected long getMaxSendStallMillis() {
		return 60_000;
	}

	@Override
	protected void onConnect() {
		log.info("Client connected: {}", getIP());
	}

	@Override
	protected void onDisconnect() {
		log.info("Client disconnected: {}", this);
	}

	public State getState() {
		return state;
	}

	public void setState(State state) {
		this.state = state;
	}

	public ChatClient getChatClient() {
		return chatClient;
	}

	public void setChatClient(ChatClient chatClient) {
		this.chatClient = chatClient;
	}

	@Override
	public String toString() {
		ChatClient chatClient = this.chatClient;
		return chatClient == null ? "Client " + getIP() : chatClient + " (" + getIP() + ")";
	}
}
