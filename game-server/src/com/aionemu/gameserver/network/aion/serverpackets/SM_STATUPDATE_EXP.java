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

	private final long currentExp;
	private final long recoverableExp;
	private final long maxExp;
	private final long curBoostExp;
	private final long maxBoostExp;

	public SM_STATUPDATE_EXP(long currentExp, long recoverableExp, long maxExp, long curBoostExp, long maxBoostExp) {
		this.currentExp = currentExp;
		this.recoverableExp = recoverableExp;
		this.maxExp = maxExp;
		this.curBoostExp = curBoostExp;
		this.maxBoostExp = maxBoostExp;
	}

	@Override
	protected void writeImpl(AionConnection con) {
		writeQ(currentExp);
		writeQ(recoverableExp);
		writeQ(maxExp);
		writeQ(curBoostExp);
		writeQ(maxBoostExp);
		// TODO better: the golden apple exp pool is not implemented, the client shows its icon while it is above zero
		writeQ(0);
	}

}
