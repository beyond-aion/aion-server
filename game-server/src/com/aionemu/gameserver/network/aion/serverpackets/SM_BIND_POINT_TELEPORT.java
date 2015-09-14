package com.aionemu.gameserver.network.aion.serverpackets;

import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;

/**
 * @author ginho1
 */
public class SM_BIND_POINT_TELEPORT extends AionServerPacket {

	int action, playerId, locId, cooldown;

	public SM_BIND_POINT_TELEPORT(int action, int playerId, int locId, int cooldown) {
		this.action = action;
		this.playerId = playerId;
		this.locId = locId;
		this.cooldown = cooldown;
	}

	@Override
	protected void writeImpl(AionConnection con) {
		writeC(action);
		writeD(playerId);
		switch (action) {
			case 1:
				writeD(locId);
				break;
			case 3:
				writeD(locId);
				writeD(cooldown);
				break;
		}

	}
}
