package com.aionemu.chatserver.common.netty;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

/**
 * @author ATracer
 */
public abstract class AbstractPacketHandler
{
	private static final Logger log = LoggerFactory.getLogger(AbstractPacketHandler.class);

	/**
	 * Unknown packet
	 * 
	 * @param id
	 * @param state
	 */
	protected void unknownPacket(int id, String state)
	{
		log.warn(String.format("Unknown packet received from Game server: 0x%02X state=%s", id, state));
	}
}
