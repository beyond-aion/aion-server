package com.aionemu.loginserver.network.aion.serverpackets;

import com.aionemu.loginserver.network.aion.AionServerPacket;
import com.aionemu.loginserver.network.aion.LoginConnection;

/**
 * @author -Nemesiss-
 */
public class SM_AUTH_GG extends AionServerPacket {

	/**
	 * Constructs new instance of <tt>SM_AUTH_GG</tt> packet. The client reads nothing from it and answers with its login (CM_LOGIN).
	 */
	public SM_AUTH_GG() {
		super(0x0b);
	}

	@Override
	protected void writeImpl(LoginConnection con) {
	}
}
