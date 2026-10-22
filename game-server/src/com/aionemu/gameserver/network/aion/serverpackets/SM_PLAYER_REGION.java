package com.aionemu.gameserver.network.aion.serverpackets;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;

/**
 * @author Rolandas
 */
public class SM_PLAYER_REGION extends AionServerPacket {

	private final int playerObjId;
	private final String subZone;

	public SM_PLAYER_REGION(Player player, String subZone) {
		this.playerObjId = player.getObjectId();
		this.subZone = subZone; // player.getActiveRegion().getZones(player).stream().findFirst().get().getZoneTemplate().getName()
	}

	@Override
	protected void writeImpl(AionConnection con) {
		writeD(playerObjId);
		writeC(0);
		writeC(0);
		writeC(0);
		writeD(subZone.hashCode()); // ???
	}
}
