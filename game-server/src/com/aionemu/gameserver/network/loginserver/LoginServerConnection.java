package com.aionemu.gameserver.network.loginserver;

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
import com.aionemu.gameserver.network.loginserver.serverpackets.SM_GS_AUTH;
import com.aionemu.gameserver.utils.ThreadPoolManager;

/**
 * Object representing connection between LoginServer and GameServer.
 * 
 * @author -Nemesiss-, Neon
 */
public class LoginServerConnection extends AConnection<LsServerPacket> {

	private static final Logger log = LoggerFactory.getLogger(LoginServerConnection.class);

	/**
	 * Possible states of GsConnection
	 */
	public enum State {
		/**
		 * game server just connect
		 */
		CONNECTED,
		/**
		 * game server is authenticated
		 */
		AUTHED
	}

	/**
	 * Server Packet "to send" Queue
	 */
	private final Deque<LsServerPacket> sendMsgQueue = new ArrayDeque<>();

	/**
	 * Current state of this connection
	 */
	private State state;

	public LoginServerConnection(SocketChannel sc, Dispatcher d) throws IOException {
		super(sc, d, 8192 * 8, 8192 * 8);
		this.state = State.CONNECTED;
	}

	@Override
	protected void initialized() {
		log.info("Connected to login server");
		sendPacket(new SM_GS_AUTH());
	}

	@Override
	protected final Queue<LsServerPacket> getSendMsgQueue() {
		return sendMsgQueue;
	}

	/**
	 * Called by Dispatcher. ByteBuffer data contains one packet that should be processed.
	 * 
	 * @param data
	 * @return True if data was processed correctly, False if some error occurred and connection should be closed NOW.
	 */
	@Override
	public boolean processData(ByteBuffer data) {
		LsClientPacket pck = LsClientPacketFactory.tryCreatePacket(data, this);

		// Execute packet only if packet exist (!= null) and read was ok.
		if (pck != null && pck.read())
			ThreadPoolManager.getInstance().execute(pck);

		return true;
	}

	/**
	 * This method will be called by Dispatcher, and will be repeated till return false.
	 * 
	 * @param data
	 * @return True if data was written to buffer, False indicating that there are not any more data to write.
	 */
	@Override
	protected final boolean writeData(ByteBuffer data) {
		synchronized (guard) {
			LsServerPacket packet = sendMsgQueue.pollFirst();
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
		log.warn("Lost connection with login server");
		LoginServer.getInstance().reconnect();
	}

	@Override
	protected final void onServerClose() {
		LoginServer.getInstance().disconnect();
	}

	/**
	 * @return Current state of this connection.
	 */
	public State getState() {
		return state;
	}

	/**
	 * @param state
	 *          Set current state of this connection.
	 */
	public void setState(State state) {
		this.state = state;
	}

	/**
	 * @return String info about this connection
	 */
	@Override
	public String toString() {
		return "Login server " + getIP();
	}
}
