package com.aionemu.gameserver.network.aion.serverpackets;

import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;

/**
 * In this packet Server is sending response for CM_RESTORE_CHARACTER.
 * 
 * @author -Nemesiss-
 */
public class SM_RESTORE_CHARACTER extends AionServerPacket {

	/**
	 * Character object id.
	 */
	private final int chaOid;
	/**
	 * True if player was restored.
	 */
	private final boolean success;

	/**
	 * Constructs new <tt>SM_RESTORE_CHARACTER </tt> packet
	 */
	public SM_RESTORE_CHARACTER(int chaOid, boolean success) {
		this.chaOid = chaOid;
		this.success = success;
	}

	@Override
	protected void writeImpl(AionConnection con) {
		writeD(success ? 0x00 : 0x10);// unk
		writeD(chaOid);
	}
}
