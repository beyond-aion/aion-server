package com.aionemu.gameserver.network.aion.serverpackets;

import com.aionemu.gameserver.model.flypath.FlyPathType;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;

/**
 * @author LokiReborn
 */
public class SM_WINDSTREAM_ANNOUNCE extends AionServerPacket {

	private final FlyPathType type;
	private final int mapId;
	private final int streamId;
	private final int state;

	public SM_WINDSTREAM_ANNOUNCE(FlyPathType type, int mapId, int streamId, int state) {
		this.type = type;
		this.mapId = mapId;
		this.streamId = streamId;
		this.state = state;
	}

	@Override
	protected void writeImpl(AionConnection con) {
		writeD(type.getId());
		writeD(mapId);
		writeD(streamId);
		writeC(state);
	}
}
