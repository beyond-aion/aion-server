package com.aionemu.gameserver.network.aion.serverpackets;

import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;
import com.aionemu.gameserver.world.WorldPosition;

/**
 * @author ATracer
 */
public class SM_CHANNEL_INFO extends AionServerPacket {

	int instanceCount = 0;
	int currentChannel = 0;

	public SM_CHANNEL_INFO(VisibleObject object) {
		WorldPosition position = object.getPosition();
		// sent before the spawn (login, teleport, channel change), when the object is not flagged as spawned yet but its region is already set
		if (position == null || position.getMapRegion() == null) {
			instanceCount = 1;
			currentChannel = 1;
		} else {
			instanceCount = position.getWorldMapInstance().getTemplate().getTwinCount();
			currentChannel = position.getInstanceId() - 1;
		}
	}

	@Override
	protected void writeImpl(AionConnection con) {
		writeD(currentChannel);
		writeD(instanceCount);
	}
}
