package com.aionemu.gameserver.network.aion.serverpackets;

import com.aionemu.gameserver.model.team.legion.LegionRank;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;

/**
 * @author sweetkr
 */
public class SM_LEGION_UPDATE_TITLE extends AionServerPacket {

	private final int playerObjectId;
	private final int legionId;
	private final String legionName;
	private final LegionRank rank;

	public SM_LEGION_UPDATE_TITLE(int playerObjectId, int legionId, String legionName, LegionRank rank) {
		this.playerObjectId = playerObjectId;
		this.legionId = legionId;
		this.legionName = legionName;
		this.rank = rank;
	}

	@Override
	protected void writeImpl(AionConnection con) {
		writeD(playerObjectId);
		writeD(legionId);
		writeS(legionName);
		writeC(rank.getRankId());
	}
}
