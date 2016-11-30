package com.aionemu.gameserver.network.aion.serverpackets;

import com.aionemu.gameserver.configs.main.SecurityConfig;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;

/**
 * @author cura
 */
public class SM_CHARACTER_SELECT extends AionServerPacket {

	private int type; // 0: new passkey input window, 1: passkey input window, 2: message window
	private short messageType; // 0: newpasskey complete, 2: passkey edit complete, 3: passkey input
	private int wrongCount;

	public SM_CHARACTER_SELECT(int type) {
		this.type = type;
	}

	public SM_CHARACTER_SELECT(int type, short messageType, int wrongCount) {
		this.type = type;
		this.messageType = messageType;
		this.wrongCount = wrongCount;
	}

	@Override
	protected void writeImpl(AionConnection con) {
		writeC(type);

		switch (type) {
			case 0:
				break;
			case 1:
				break;
			case 2:
				writeH(messageType); // 0: newpasskey complete, 2: passkey edit complete, 3: passkey input
				writeC(wrongCount > 0 ? 1 : 0); // 0: right passkey, 1: wrong passkey
				writeD(wrongCount); // wrong passkey input count
				writeD(SecurityConfig.PASSKEY_WRONG_MAXCOUNT); // Enter the number of possible wrong numbers (retail
				// server default value: 5)
				break;
		}
	}
}
