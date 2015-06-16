package com.aionemu.gameserver.network.aion.serverpackets;

import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;
import com.aionemu.gameserver.world.zone.ZoneName;

/**
 * @author Rolandas
 */
public class SM_PLAYER_REGION extends AionServerPacket {

	private final ZoneName subZone;

	public SM_PLAYER_REGION(ZoneName subZone) {
		this.subZone = subZone;
	}

	@Override
	protected void writeImpl(AionConnection con) {
		writeD(con.getActivePlayer().getObjectId());
		writeC(0);
		writeC(0);
		writeC(0);
		writeD(subZone.name().hashCode());
	}
}
