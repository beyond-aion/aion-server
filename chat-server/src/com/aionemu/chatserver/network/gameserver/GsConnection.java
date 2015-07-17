package com.aionemu.chatserver.network.gameserver;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.ArrayDeque;
import java.util.Deque;

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
public class GsConnection extends AConnection
{
	private static final Logger log = LoggerFactory.getLogger(GsConnection.class);
	private final Deque<GsServerPacket> sendMsgQueue = new ArrayDeque<GsServerPacket>();
	private State state;
	
	public static enum State
	{
		CONNECTED,
		AUTHED
	}
	
	public GsConnection(SocketChannel sc, Dispatcher d) throws IOException
	{
		super(sc, d, 8192*8, 8192*8);
	}

	@Override
	public boolean processData(ByteBuffer data)
	{
		GsClientPacket pck = GsPacketHandlerFactory.handle(data, this);
		if (pck != null && pck.read())
			ThreadPoolManager.getInstance().execute(pck);
		return true;
	}

	@Override
	protected final boolean writeData(ByteBuffer data)
	{
		synchronized (guard)
		{
			GsServerPacket packet = sendMsgQueue.pollFirst();
			if (packet == null)
				return false;
			packet.write(this, data);
			return true;
		}
	}

	@Override
	protected final long getDisconnectionDelay()
	{
		return 0;
	}

	@Override
	protected final void onDisconnect()
	{
		GameServerService.getInstance().setOffline();
	}

	@Override
	protected final void onServerClose()
	{
		close(true);
	}

	public final void sendPacket(GsServerPacket bp)
	{
		synchronized (guard)
		{
			if (isWriteDisabled())
				return;
			sendMsgQueue.addLast(bp);
			enableWriteInterest();
		}
	}

	public final void close(GsServerPacket closePacket, boolean forced)
	{
		synchronized (guard)
		{
			if (isWriteDisabled())
				return;
			pendingClose = true;
			isForcedClosing = forced;
			sendMsgQueue.clear();
			sendMsgQueue.addLast(closePacket);
			enableWriteInterest();
		}
	}

	public State getState()
	{
		return state;
	}

	public void setState(State state)
	{
		this.state = state;
	}

	@Override
	protected void initialized()
	{
		state = State.CONNECTED;
		log.info("Gameserver connection attemp from: " + getIP());
	}
}
