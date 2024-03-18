package com.aionemu.loginserver.network.gameserver;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.network.AConnection;
import com.aionemu.commons.network.Dispatcher;
import com.aionemu.loginserver.GameServerInfo;
import com.aionemu.loginserver.PingPongTask;
import com.aionemu.loginserver.controller.AccountController;
import com.aionemu.loginserver.network.factories.GsPacketHandlerFactory;

/**
 * Object representing connection between LoginServer and GameServer.
 * 
 * @author -Nemesiss-
 */
public class GsConnection extends AConnection<GsServerPacket> {

	/**
	 * Logger for this class.
	 */
	private static final Logger log = LoggerFactory.getLogger(GsConnection.class);
	private static final ExecutorService PACKET_EXECUTOR = Executors.newCachedThreadPool();
	private static final ScheduledExecutorService PINGPONG_EXECUTOR = Executors.newSingleThreadScheduledExecutor();

	static {
		((ThreadPoolExecutor) PACKET_EXECUTOR).setCorePoolSize(1);
	}

	/**
	 * Possible states of GsConnection
	 */
	public enum State {
		/**
		 * Means that GameServer just connect, but is not authenticated yet
		 */
		CONNECTED,
		/**
		 * GameServer is authenticated
		 */
		AUTHED
	}

	/**
	 * Server Packet "to send" Queue
	 */
	private final Deque<GsServerPacket> sendMsgQueue = new ArrayDeque<>();

	/**
	 * Current state of this connection
	 */
	private State state;

	/**
	 * GameServerInfo for this GsConnection.
	 */
	private GameServerInfo gameServerInfo = null;

	private final PingPongTask pingPongTask = new PingPongTask(this);

	/**
	 * Constructor.
	 * 
	 * @param sc
	 * @param d
	 * @throws IOException
	 */
	public GsConnection(SocketChannel sc, Dispatcher d) throws IOException {
		super(sc, d, 8192 * 8, 8192 * 8);
	}

	@Override
	protected final Queue<GsServerPacket> getSendMsgQueue() {
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
		GsClientPacket pck = GsPacketHandlerFactory.handle(data, this);

		if (pck != null && pck.read())
			PACKET_EXECUTOR.execute(pck);

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
			GsServerPacket packet = sendMsgQueue.pollFirst();
			if (packet == null)
				return false;

			packet.write(this, data);
			return true;
		}
	}

	@Override
	protected final void onDisconnect() {
		pingPongTask.stop();
		log.info(this + " disconnected");
		if (gameServerInfo != null) {
			gameServerInfo.setConnection(null);
			gameServerInfo.clearAccountsOnGameServer();
			gameServerInfo = null;
		}
		AccountController.updateServerListForAllLoggedInPlayers();
	}

	@Override
	protected final void onServerClose() {
		close();
		PINGPONG_EXECUTOR.shutdown();
		PACKET_EXECUTOR.shutdown();
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
		if (state == State.AUTHED) {
			pingPongTask.start(PINGPONG_EXECUTOR);
		}
	}

	/**
	 * @return GameServerInfo for this GsConnection or null if this GsConnection is not authenticated yet.
	 */
	public GameServerInfo getGameServerInfo() {
		return gameServerInfo;
	}

	/**
	 * @param gameServerInfo
	 *          Set GameServerInfo for this GsConnection.
	 */
	public void setGameServerInfo(GameServerInfo gameServerInfo) {
		this.gameServerInfo = gameServerInfo;
	}

	public PingPongTask getPingPongTask() {
		return pingPongTask;
	}

	/**
	 * @return String info about this connection
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("Gameserver");
		if (gameServerInfo != null)
			sb.append(" #").append(gameServerInfo.getId());
		sb.append(" ").append(getIP());
		return sb.toString();
	}

	@Override
	protected void initialized() {
		state = State.CONNECTED;
		log.info("Gameserver connection attempt from: " + getIP());
	}
}
