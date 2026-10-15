package com.aionemu.gameserver.network.aion.serverpackets;

import com.aionemu.gameserver.model.ClientBroadcastType;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;

/**
 * @author xTz
 */
public class SM_GROUP_DATA_EXCHANGE extends AionServerPacket {

	private final ClientBroadcastType type;
	private final int tag;
	private final byte[] data;

	/**
	 * @param tag
	 *          the client's own tag of team data, passed on unchanged
	 */
	public SM_GROUP_DATA_EXCHANGE(int tag, byte[] data) {
		this(ClientBroadcastType.TEAM, tag, data);
	}

	/**
	 * House data, which always goes out as {@link ClientBroadcastType#HOUSE}, whichever house type it was sent as.
	 */
	public SM_GROUP_DATA_EXCHANGE(byte[] data) {
		this(ClientBroadcastType.HOUSE, 0, data);
	}

	private SM_GROUP_DATA_EXCHANGE(ClientBroadcastType type, int tag, byte[] data) {
		this.type = type;
		this.tag = tag;
		this.data = data;
	}

	@Override
	protected void writeImpl(AionConnection con) {
		writeC(type.getId());
		if (type == ClientBroadcastType.TEAM)
			writeC(tag);
		writeD(data.length);
		writeB(data);
	}
}
