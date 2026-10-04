package com.aionemu.chatserver.network.aion;

import java.nio.ByteBuffer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.network.packet.BaseClientPacket;

/**
 * @author ATracer
 */
public abstract class AbstractClientPacket extends BaseClientPacket<AionConnection> {

	private static final Logger log = LoggerFactory.getLogger(AbstractClientPacket.class);

	public AbstractClientPacket(ByteBuffer buf, AionConnection connection, int opCode) {
		super(buf, opCode);
		setConnection(connection);
	}

	@Override
	public final void run() {
		try {
			runImpl();
		} catch (Exception e) {
			log.error("Could not execute {} from {}", this, getConnection(), e);
		}
	}
}
