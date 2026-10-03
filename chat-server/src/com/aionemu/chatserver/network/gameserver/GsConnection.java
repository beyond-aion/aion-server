package com.aionemu.chatserver.network.gameserver;

import java.nio.ByteBuffer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.chatserver.network.factories.GsPacketHandlerFactory;
import com.aionemu.chatserver.network.netty.NettyConnection;
import com.aionemu.chatserver.service.GameServerService;
import com.aionemu.commons.network.packet.BaseClientPacket;

import io.netty.channel.Channel;
import io.netty.util.concurrent.EventExecutor;

/**
 * @author KID
 */
public class GsConnection extends NettyConnection<GsServerPacket> {

	private static final Logger log = LoggerFactory.getLogger(GsConnection.class);

	private volatile GameServerConnectionState state = GameServerConnectionState.CONNECTED;

	public enum GameServerConnectionState {
		CONNECTED,
		AUTHED
	}

	public GsConnection(Channel channel, EventExecutor packetExecutor) {
		super(channel, packetExecutor);
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
	protected void onConnect() {
		log.info("Game server connected: {}", getIP());
	}

	@Override
	protected void onDisconnect() {
		GameServerService.getInstance().setOffline();
	}

	public GameServerConnectionState getState() {
		return state;
	}

	public void setState(GameServerConnectionState state) {
		this.state = state;
	}

	@Override
	public String toString() {
		return "Game server " + getIP();
	}
}
