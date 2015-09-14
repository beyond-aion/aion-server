package com.aionemu.gameserver.network.aion.serverpackets;

import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;

/**
 * @author alexa026
 */
public class SM_LOOKATOBJECT extends AionServerPacket {

	private VisibleObject visibleObject;
	private int targetObjectId;
	private int heading;

	public SM_LOOKATOBJECT(VisibleObject visibleObject) {
		this.visibleObject = visibleObject;
		if (visibleObject.getTarget() != null) {
			this.targetObjectId = visibleObject.getTarget().getObjectId();
			this.heading = Math.abs(128 - visibleObject.getTarget().getHeading());
		} else {
			this.targetObjectId = 0;
			this.heading = visibleObject.getHeading();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void writeImpl(AionConnection con) {
		writeD(visibleObject.getObjectId());
		writeD(targetObjectId);
		writeC(heading);
	}
}
