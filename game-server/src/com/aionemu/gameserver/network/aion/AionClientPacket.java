package com.aionemu.gameserver.network.aion;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.network.packet.BaseClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection.State;

/**
 * Base class for every Aion -> GS Client Packet
 * 
 * @author -Nemesiss-
 */
public abstract class AionClientPacket extends BaseClientPacket<AionConnection> {

	private static final Logger log = LoggerFactory.getLogger(AionClientPacket.class);

	private final Set<State> validStates;

	/**
	 * Constructs new client packet instance. ByteBuffer and ClientConnection should be later set manually, after using this constructor.
	 * 
	 * @param opcode
	 *          packet id
	 * @param validStates
	 *          connection valid states
	 */
	protected AionClientPacket(int opcode, Set<State> validStates) {
		super(opcode);
		this.validStates = validStates;
	}

	@Override
	public final void run() {
		try {
			if (isValid()) // run only if packet is still valid (connection state didn't change, for example due to logout)
				runImpl();
		} catch (Throwable e) {
			log.error("Error handling client packet from " + getConnection() + ": " + this, e);
		}
	}

	/**
	 * Send new AionServerPacket to connection that is owner of this packet. This method is equivalent to: getConnection().sendPacket(msg);
	 * 
	 * @param msg
	 */
	protected void sendPacket(AionServerPacket msg) {
		getConnection().sendPacket(msg);
	}

	/**
	 * Reads a fixed size string from the buffer, omitting not present trailing characters for the return value.
	 */
	protected final String readS(int characterCount) {
		String string = readS(); // read byte length = characters * 2 + 2
		if (string.length() < characterCount)
			readB((characterCount - string.length()) * 2);
		return string;
	}

	/**
	 * Checks if the packet is still valid for its connection.
	 * 
	 * @return True if packet is still valid and should be processed.
	 */
	public final boolean isValid() {
		return validStates.contains(getConnection().getState());
	}

	@Override
	protected int getOpCodeZeroPadding() {
		return 3;
	}
}
