package com.aionemu.chatserver.network.gameserver;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.chatserver.network.factories.GsPacketHandlerFactory;
import com.aionemu.chatserver.service.GameServerService;
import com.aionemu.commons.network.AConnection;
import com.aionemu.commons.network.Dispatcher;

/**
 * @author KID
 */
public class GsConnection extends AConnection<GsServerPacket> {

	private static final Logger log = LoggerFactory.getLogger(GsConnection.class);
	private static final ExecutorService PACKET_EXECUTOR = Executors.newCachedThreadPool();
	private final Deque<GsServerPacket> sendMsgQueue = new ArrayDeque<>();
	private GameServerConnectionState state;

	static {
		((ThreadPoolExecutor) PACKET_EXECUTOR).setCorePoolSize(1);
	}

	public enum GameServerConnectionState {

		CONNECTED,
		AUTHED

	}

	public GsConnection(SocketChannel sc, Dispatcher d) throws IOException {
		super(sc, d, 8192 * 8, 8192 * 8);
	}

	@Override
	protected final Queue<GsServerPacket> getSendMsgQueue() {
		return sendMsgQueue;
	}

	@Override
	public boolean processData(ByteBuffer data) {
		GsClientPacket pck = GsPacketHandlerFactory.handle(data, this);
		if (pck != null && pck.read())
			PACKET_EXECUTOR.execute(pck);
		return true;
	}

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
		GameServerService.getInstance().setOffline();
	}

	@Override
	protected final void onServerClose() {
		close();
		PACKET_EXECUTOR.shutdown();
	}

	public GameServerConnectionState getState() {
		return state;
	}

	public void setState(GameServerConnectionState state) {
		this.state = state;
	}

	@Override
	public String toString() {
		return "Gameserver " + getIP();
	}

	@Override
	protected void initialized() {
		state = GameServerConnectionState.CONNECTED;
		log.info("Gameserver connection attempt from: {}", getIP());
	}
}
