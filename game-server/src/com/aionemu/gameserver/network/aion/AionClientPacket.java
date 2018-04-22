package com.aionemu.gameserver.network.aion;

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

	/**
	 * run runImpl catching and logging Throwable.
	 */
	@Override
	public final void run() {
		try {
			if (!isValid()) // run only if packet is still valid (connection state didn't change, for example due to logout)
				return;
			if (isForbidden()) {
				getConnection().sendPacket(SM_SYSTEM_MESSAGE.STR_MSG_ACCUSE_TARGET_IS_NOT_VALID());
				return;
			}
			runImpl();
		} catch (Throwable e) {
			String name = getConnection().getAccount() == null ? getConnection().getIP() : getConnection().getAccount().toString();
			log.error("Error handling client packet from " + name + ": " + this, e);
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

	@Override
	public String toFormattedPacketNameString() {
		return String.format("[0x%03X] %s", getOpCode(), getPacketName());
	}
}
