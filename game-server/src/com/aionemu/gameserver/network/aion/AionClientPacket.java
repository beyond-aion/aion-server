package com.aionemu.gameserver.network.aion;

import java.util.EnumSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.network.packet.BaseClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection.State;

/**
 * Base class for every Aion -> LS Client Packet
 * 
 * @author -Nemesiss-
 */
public abstract class AionClientPacket extends BaseClientPacket<AionConnection> implements Cloneable {

	/**
	 * Logger for this class.
	 */
	private static final Logger log = LoggerFactory.getLogger(AionClientPacket.class);

	private final Set<State> validStates;

	/**
	 * Constructs new client packet instance. ByBuffer and ClientConnection should be later set manually, after using this constructor.
	 * 
	 * @param opcode
	 *          packet id
	 * @param state
	 *          connection valid state
	 * @param restStates
	 *          rest of connection valid state (optional - if there are more than one)
	 */
	protected AionClientPacket(int opcode, State state, State... restStates) {
		super(opcode);
		validStates = EnumSet.of(state, restStates);
	}

	/**
	 * run runImpl catching and logging Throwable.
	 */
	@Override
	public final void run() {

		try {
			// run only if packet is still valid (connection state didn't changed)
			if (isValid())
				runImpl();
		} catch (Throwable e) {
			String name = getConnection().getAccount().getName();
			if (name == null)
				name = getConnection().getIP();

			log.error("Error handling client (" + name + ") message :" + this, e);
		}
	}

	/**
	 * Send new AionServerPacket to connection that is owner of this packet. This method is equvalent to: getConnection().sendPacket(msg);
	 * 
	 * @param msg
	 */
	protected void sendPacket(AionServerPacket msg) {
		getConnection().sendPacket(msg);
	}

	/**
	 * Clones this packet object.
	 * 
	 * @return AionClientPacket
	 */
	public AionClientPacket clonePacket() {
		try {
			return (AionClientPacket) super.clone();
		} catch (CloneNotSupportedException e) {
			return null;
		}
	}

	protected final String readS(int size) {
		String string = readS();
		if (string != null)
			readB(size - (string.length() * 2 + 2));
		else
			readB(size);
		return string;
	}

	/**
	 * Check if packet is still valid for its connection.
	 * 
	 * @return true if packet is still valid and should be processed.
	 */
	public final boolean isValid() {
		State state = getConnection().getState();
		boolean valid = validStates.contains(state);

		if (!valid)
			log.info(this + " won't be processed because the connections current state (" + state + ") is invalid");
		return valid;
	}
}
