package com.aionemu.gameserver.network.aion.serverpackets;

import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;

/**
 * @author ATracer
 */
public class SM_CHANNEL_INFO extends AionServerPacket {

	private final int instanceCount;
	private final int currentChannel;

	public SM_CHANNEL_INFO(VisibleObject object) {
		instanceCount = object.getWorldMapInstance().getParent().getInstanceCount();
		currentChannel = object.getInstanceId() - 1;
	}

	@Override
	protected void writeImpl(AionConnection con) {
		writeD(currentChannel);
		writeD(instanceCount);
	}
}
