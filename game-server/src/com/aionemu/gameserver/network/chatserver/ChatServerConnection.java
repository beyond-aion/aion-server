package com.aionemu.gameserver.network.chatserver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.network.netty.NettyConnection;
import com.aionemu.commons.network.packet.BaseClientPacket;
import com.aionemu.gameserver.GameServer;
import com.aionemu.gameserver.network.chatserver.serverpackets.SM_CS_AUTH;
import com.aionemu.gameserver.utils.ThreadPoolManager;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;

/**
 * Connection to the chat server. Its packets are executed concurrently.
 *
 * @author ATracer, Neon
 */
public class ChatServerConnection extends NettyConnection<CsServerPacket> {

	private static final Logger log = LoggerFactory.getLogger(ChatServerConnection.class);

	/**
	 * Possible states of CsConnection
	 */
	public enum State {
		/**
		 * chat server just connected
		 */
		CONNECTED,
		/**
		 * chat server is authenticated
		 */
		AUTHED
	}

	/**
	 * Current state of this connection
	 */
	private volatile State state = State.CONNECTED;

	public ChatServerConnection(Channel channel) {
		super(channel, ThreadPoolManager.getInstance());
	}

	@Override
	protected void onConnect() {
		log.info("Connected to chat server");
		sendPacket(new SM_CS_AUTH());
	}

	@Override
	protected BaseClientPacket<?> createPacket(ByteBuf data) {
		return CsClientPacketFactory.tryCreatePacket(data, this);
	}

	@Override
	protected void writePacket(CsServerPacket packet, ByteBuf buffer) {
		packet.write(this, buffer);
	}

	@Override
	protected boolean isReadFailureFatal() {
		return false;
	}

	@Override
	protected int getMaxPendingPackets() {
		return 10_000;
	}

	@Override
	protected final void onDisconnect() {
		if (GameServer.isShutdownScheduled())
			return;
		log.warn("Lost connection with chat server");
		ChatServer.getInstance().reconnect();
	}

	public State getState() {
		return state;
	}

	public void setState(State state) {
		this.state = state;
	}

	@Override
	public String toString() {
		return "Chat server " + getIP();
	}
}
