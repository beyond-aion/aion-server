package com.aionemu.gameserver.network.loginserver.clientpackets;

import com.aionemu.gameserver.network.BannedMacManager;
import com.aionemu.gameserver.network.loginserver.LsClientPacket;

/**
 * 
 * @author KID
 *
 */
public class CM_MACBAN_LIST extends LsClientPacket {

	public CM_MACBAN_LIST(int opCode) {
		super(opCode);
	}

	@Override
	protected void readImpl() {
		BannedMacManager bmm = BannedMacManager.getInstance();
		int cnt = readD();
		for(int a = 0; a < cnt; a++) {
			bmm.dbLoad(readS(), readQ(), readS());
		}
		
		bmm.onEnd();
	}

	@Override
	protected void runImpl() {
		// ?
	}
}
