package com.aionemu.loginserver.network.gameserver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.network.packet.BaseClientPacket;

/**
 * Base class for every GameServer -> LS Client Packet
 * 
 * @author -Nemesiss-
 */
public abstract class GsClientPacket extends BaseClientPacket<GsConnection> {

	public GsClientPacket() {
		super(0);
	}

	/**
	 * Logger for this class.
	 */
	private static final Logger log = LoggerFactory.getLogger(GsClientPacket.class);

	/**
	 * run runImpl catching and logging Throwable.
	 */
	@Override
	public final void run() {
		try {
			runImpl();
		} catch (Throwable e) {
			log.warn("error handling gs (" + getConnection().getIP() + ") message " + this, e);
		}
	}

	/**
	 * Send new GsServerPacket to connection that is owner of this packet. This method is equivalent to: getConnection().sendPacket(msg);
	 * 
	 * @param msg
	 */
	protected void sendPacket(GsServerPacket msg) {
		getConnection().sendPacket(msg);
	}
}
