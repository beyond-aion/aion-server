package com.aionemu.gameserver.network.loginserver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.network.netty.NettyConnection;
import com.aionemu.commons.network.packet.BaseClientPacket;
import com.aionemu.gameserver.GameServer;
import com.aionemu.gameserver.network.loginserver.serverpackets.SM_GS_AUTH;
import com.aionemu.gameserver.utils.ThreadPoolManager;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;

/**
 * Object representing connection between LoginServer and GameServer. Its packets are executed concurrently.
 *
 * @author -Nemesiss-, Neon
 */
public class LoginServerConnection extends NettyConnection<LsServerPacket> {

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
	 * Current state of this connection
	 */
	private volatile State state = State.CONNECTED;

	public LoginServerConnection(Channel channel) {
		super(channel, ThreadPoolManager.getInstance());
	}

	@Override
	protected void onConnect() {
		log.info("Connected to login server");
		sendPacket(new SM_GS_AUTH());
	}

	@Override
	protected BaseClientPacket<?> createPacket(ByteBuf data) {
		return LsClientPacketFactory.tryCreatePacket(data, this);
	}

	@Override
	protected void writePacket(LsServerPacket packet, ByteBuf buffer) {
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
		log.warn("Lost connection with login server");
		LoginServer.getInstance().reconnect();
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
