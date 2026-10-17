package com.aionemu.loginserver.network.aion;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.network.packet.BaseClientPacket;
import com.aionemu.loginserver.model.Account;

import io.netty.buffer.ByteBuf;

/**
 * Base class for every Aion -> LS Client Packet
 * 
 * @author -Nemesiss-
 */
public abstract class AionClientPacket extends BaseClientPacket<LoginConnection> {

	/**
	 * Logger for this class.
	 */
	private static final Logger log = LoggerFactory.getLogger(AionClientPacket.class);

	/**
	 * Constructs new client packet.
	 * 
	 * @param buf
	 *          packet data
	 * @param client
	 *          client
	 * @param opcode
	 *          packet id
	 */
	protected AionClientPacket(ByteBuf buf, LoginConnection client, int opcode) {
		super(buf, opcode);
		setConnection(client);
	}

	/**
	 * Reads the fields, then the padding to whole Blowfish blocks (up to 7 bytes) that the client puts between them and the checksum. Anything
	 * longer stays unread and gets reported.
	 */
	@Override
	protected final void readImpl() {
		readFields();
		if (getRemainingBytes() < 8)
			readB(getRemainingBytes());
	}

	protected abstract void readFields();

	/**
	 * run runImpl catching and logging Throwable.
	 */
	@Override
	public final void run() {
		try {
			runImpl();
		} catch (Throwable e) {
			String name;
			Account account = getConnection().getAccount();
			if (account != null) {
				name = account.getName();
			} else {
				name = getConnection().getIP();
			}

			log.error("error handling client ({}) message {}", name, this, e);
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
}
