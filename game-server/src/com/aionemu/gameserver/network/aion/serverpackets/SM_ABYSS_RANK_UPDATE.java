package com.aionemu.gameserver.network.aion.serverpackets;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;

/**
 * @author Nemiroff Date: 17.02.2010
 */
// TODO Rename
public class SM_ABYSS_RANK_UPDATE extends AionServerPacket {

	private final Player player;
	private final int action;

	public SM_ABYSS_RANK_UPDATE(int action, Player player) {
		this.action = action;
		this.player = player;
	}

	@Override
	protected void writeImpl(AionConnection con) {
		writeC(action);
		writeD(player.getObjectId());
		switch (action) {
			case 0: // Abyss rank change
				writeD(player.getAbyssRank().getRank().getId());
				break;
			case 1: // Team objectId
				writeD(player.getCurrentTeamId());
				break;
			case 2: // Mentor status change
				if (player.isMentor())
					writeD(1);
				else
					writeD(0);
		}
	}

}
