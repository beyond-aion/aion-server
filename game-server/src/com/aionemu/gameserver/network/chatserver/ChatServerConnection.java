package com.aionemu.gameserver.network.chatserver;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Queue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.network.AConnection;
import com.aionemu.commons.network.Dispatcher;
import com.aionemu.gameserver.GameServer;
import com.aionemu.gameserver.network.chatserver.serverpackets.SM_CS_AUTH;
import com.aionemu.gameserver.utils.ThreadPoolManager;

/**
 * @author ATracer, Neon
 */
public class ChatServerConnection extends AConnection<CsServerPacket> {

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
	 * Server Packet "to send" Queue
	 */
	private final Deque<CsServerPacket> sendMsgQueue = new ArrayDeque<>();

	/**
	 * Current state of this connection
	 */
	private State state;

	public ChatServerConnection(SocketChannel sc, Dispatcher d) throws IOException {
		super(sc, d, 8192 * 2, 8192 * 2);
		this.state = State.CONNECTED;
	}

	@Override
	protected void initialized() {
		log.info("Connected to chat server");
		sendPacket(new SM_CS_AUTH());
	}

	@Override
	protected final Queue<CsServerPacket> getSendMsgQueue() {
		return sendMsgQueue;
	}

	@Override
	public boolean processData(ByteBuffer data) {
		CsClientPacket pck = CsClientPacketFactory.tryCreatePacket(data, this);

		// Execute packet only if packet exist (!= null) and read was ok.
		if (pck != null && pck.read())
			ThreadPoolManager.getInstance().execute(pck);

		return true;
	}

	@Override
	protected final boolean writeData(ByteBuffer data) {
		synchronized (guard) {
			CsServerPacket packet = sendMsgQueue.pollFirst();
			if (packet == null)
				return false;

			packet.write(this, data);
			return true;
		}
	}

	@Override
	protected final void onDisconnect() {
		if (GameServer.isShutdownScheduled())
			return;
		log.warn("Lost connection with chat server");
		ChatServer.getInstance().reconnect();
	}

	@Override
	protected final void onServerClose() {
		ChatServer.getInstance().disconnect();
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
