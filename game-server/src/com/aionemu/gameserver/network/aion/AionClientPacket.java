package com.aionemu.gameserver.network.aion;

import java.util.EnumSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.network.packet.BaseClientPacket;
import com.aionemu.gameserver.configs.main.SecurityConfig;
import com.aionemu.gameserver.network.aion.AionConnection.State;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;

/**
 * Base class for every Aion -> GS Client Packet
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
			if (!isValid()) {
				log.info(this + " won't be processed because the connections current state (" + getConnection().getState() + ") is invalid");
				return;
			}
			if (isForbidden()) {
				getConnection().sendPacket(SM_SYSTEM_MESSAGE.STR_MSG_ACCUSE_TARGET_IS_NOT_VALID());
				return;
			}
			runImpl();
		} catch (Throwable e) {
			String name = getConnection().getAccount() == null ? getConnection().getIP() : getConnection().getAccount().toString();
			log.error("Error handling client (" + name + ") message :" + this, e);
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
	 * Checks if the packet is still valid for its connection.
	 * 
	 * @return True if packet is still valid and should be processed.
	 */
	public final boolean isValid() {
		return validStates.contains(getConnection().getState());
	}

	/**
	 * Checks if the packet is allowed to be processed for the current user.
	 * 
	 * @return True if it is a forbidden packet for this connection.
	 */
	public final boolean isForbidden() {
		if (SecurityConfig.HDD_SERIAL_HACKED_ACCOUNTS_FORBIDDEN_PACKETS.isEmpty())
			return false;
		if (getConnection().getAccount() == null)
			return false;
		return getConnection().getAccount().isHacked() && SecurityConfig.HDD_SERIAL_HACKED_ACCOUNTS_FORBIDDEN_PACKETS.contains(getClass());
	}
}
