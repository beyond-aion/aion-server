package com.aionemu.chatserver.network.gameserver;

import java.nio.ByteBuffer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.network.packet.BaseClientPacket;

/**
 * @author KID
 */
public abstract class GsClientPacket extends BaseClientPacket<GsConnection> {

	private static final Logger log = LoggerFactory.getLogger(GsClientPacket.class);

	public GsClientPacket(ByteBuffer buffer, GsConnection connection, int opCode) {
		super(opCode);
	}

	@Override
	public final void run() {
		try {
			runImpl();
		} catch (Throwable e) {
			log.warn("error handling gs ({}) message {}", getConnection().getIP(), this, e);
		}
	}

	protected void sendPacket(GsServerPacket msg) {
		getConnection().sendPacket(msg);
	}
}
