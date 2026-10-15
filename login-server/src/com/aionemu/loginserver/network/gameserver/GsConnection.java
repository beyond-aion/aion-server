package com.aionemu.loginserver.network.gameserver;

import java.nio.ByteBuffer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.network.netty.NettyConnection;
import com.aionemu.commons.network.packet.BaseClientPacket;
import com.aionemu.loginserver.GameServerInfo;
import com.aionemu.loginserver.PingPongTask;
import com.aionemu.loginserver.controller.AccountController;
import com.aionemu.loginserver.network.factories.GsPacketHandlerFactory;

import io.netty.channel.Channel;

/**
 * Object representing connection between LoginServer and GameServer.
 * 
 * @author -Nemesiss-
 */
public class GsConnection extends NettyConnection<GsServerPacket> {

	/**
	 * Logger for this class.
	 */
	private static final Logger log = LoggerFactory.getLogger(GsConnection.class);
	/**
	 * Packets of game servers are executed concurrently, not one after another.
	 */
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
	 * Current state of this connection
	 */
	private volatile State state = State.CONNECTED;

	/**
	 * GameServerInfo for this GsConnection.
	 */
	private GameServerInfo gameServerInfo = null;

	private final PingPongTask pingPongTask = new PingPongTask(this);

	public GsConnection(Channel channel) {
		super(channel, PACKET_EXECUTOR);
	}

	@Override
	protected BaseClientPacket<?> createPacket(ByteBuffer data) {
		return GsPacketHandlerFactory.handle(data, this);
	}

	@Override
	protected void writePacket(GsServerPacket packet, ByteBuffer buffer) {
		packet.write(this, buffer);
	}

	@Override
	protected int getMaxPendingPackets() {
		return 10_000;
	}

	@Override
	protected final void onDisconnect() {
		pingPongTask.stop();
		log.info("{} disconnected", this);
		if (gameServerInfo != null) {
			gameServerInfo.setConnection(null);
			gameServerInfo.clearAccountsOnGameServer();
			gameServerInfo = null;
		}
		AccountController.updateServerListForAllLoggedInPlayers();
	}

	/**
	 * Stops the executors of all game server connections. Call after their channels were closed.
	 */
	public static void shutdownExecutors() {
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
		StringBuilder sb = new StringBuilder("Game server");
		if (gameServerInfo != null)
			sb.append(" #").append(gameServerInfo.getId());
		sb.append(" ").append(getIP());
		return sb.toString();
	}

	@Override
	protected boolean isAuthenticated() {
		return state == State.AUTHED;
	}

	@Override
	protected long getAuthTimeoutMillis() {
		return 30_000;
	}

	@Override
	protected void onConnect() {
		log.info("Game server connected: {}", getIP());
	}
}
