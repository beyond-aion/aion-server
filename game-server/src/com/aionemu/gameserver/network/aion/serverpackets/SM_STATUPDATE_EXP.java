package com.aionemu.gameserver.network.aion.serverpackets;

import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;

/**
 * This packet is used to update current exp / recoverable exp / max exp values.
 * 
 * @author Luno
 * @updated by alexa026
 */
public class SM_STATUPDATE_EXP extends AionServerPacket {

	private long currentExp;
	private long recoverableExp;
	private long maxExp;

	private long curBoostExp = 0;
	private long maxBoostExp = 0;

	/**
	 * @param currentExp
	 * @param recoverableExp
	 * @param maxExp
	 */
	public SM_STATUPDATE_EXP(long currentExp, long recoverableExp, long maxExp, long rep1, long rep2) {
		this.currentExp = currentExp;
		this.recoverableExp = recoverableExp;
		this.maxExp = maxExp;
		curBoostExp = rep1;
		maxBoostExp = rep2;
	}

	@Override
	protected void writeImpl(AionConnection con) {
		writeQ(currentExp);
		writeQ(recoverableExp);
		writeQ(maxExp);
		writeQ(curBoostExp);
		writeQ(maxBoostExp);
	}

}
