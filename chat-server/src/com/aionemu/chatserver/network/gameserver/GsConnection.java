package com.aionemu.chatserver.network.gameserver;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Queue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.chatserver.network.factories.GsPacketHandlerFactory;
import com.aionemu.chatserver.service.GameServerService;
import com.aionemu.commons.network.AConnection;
import com.aionemu.commons.network.Dispatcher;
import com.aionemu.commons.network.util.ThreadPoolManager;

/**
 * @author KID
 */
public class GsConnection extends AConnection<GsServerPacket> {

	private static final Logger log = LoggerFactory.getLogger(GsConnection.class);
	private final Deque<GsServerPacket> sendMsgQueue = new ArrayDeque<>();
	private State state;

	public static enum State {
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
			ThreadPoolManager.getInstance().execute(pck);
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
	}

	public State getState() {
		return state;
	}

	public void setState(State state) {
		this.state = state;
	}

	@Override
	protected void initialized() {
		state = State.CONNECTED;
		log.info("Gameserver connection attempt from: " + getIP());
	}
}
