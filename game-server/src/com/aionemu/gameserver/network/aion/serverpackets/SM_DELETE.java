package com.aionemu.gameserver.network.aion.serverpackets;

import com.aionemu.gameserver.model.gameobjects.AionObject;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;
import com.aionemu.gameserver.world.knownlist.KnownList.DeleteType;

/**
 * This packet is informing client that some AionObject is no longer visible.
 *
 * @author -Nemesiss-
 */
public class SM_DELETE extends AionServerPacket {

	/**
	 * Object that is no longer visible.
	 */
	private final int objectId;
	private final int type;

	/**
	 * Constructor.
	 *
	 * @param object
	 * @param type
	 */
	public SM_DELETE(AionObject object, DeleteType type) {
		this.objectId = object.getObjectId();
		this.type = type.getType();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void writeImpl(AionConnection con) {
		int action = 0;
		if (action != 1) {
			writeD(objectId);
			writeC(type);
		}
	}

}