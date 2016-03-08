package com.aionemu.gameserver.network.aion.serverpackets;

import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;

/**
 * @author Nemesiss
 */
public class SM_HEADING_UPDATE extends AionServerPacket {

	private int objectId;
	private byte heading;

	public SM_HEADING_UPDATE(VisibleObject target) {
		this.objectId = target.getObjectId();
		this.heading = target.getHeading();
	}

	@Override
	protected void writeImpl(AionConnection con) {
		writeD(objectId);
		writeC(heading);
	}
}
