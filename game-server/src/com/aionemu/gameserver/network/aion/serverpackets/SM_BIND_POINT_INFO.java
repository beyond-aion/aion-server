package com.aionemu.gameserver.network.aion.serverpackets;

import com.aionemu.gameserver.model.gameobjects.Kisk;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;
import com.aionemu.gameserver.world.WorldPosition;

/*
 * @author sweetkr, Sarynth, Sykra
 */
public class SM_BIND_POINT_INFO extends AionServerPacket {

	private final int mapId;
	private final float x;
	private final float y;
	private final float z;
	private final byte bindPointType;
	private final int kiskObjId;

	public SM_BIND_POINT_INFO(int mapId, float x, float y, float z) {
		this.mapId = mapId;
		this.x = x;
		this.y = y;
		this.z = z;
		this.bindPointType = 0;
		this.kiskObjId = 0;
	}

	public SM_BIND_POINT_INFO(Kisk kisk) {
		WorldPosition pos = kisk.getPosition();
		this.mapId = pos.getMapId();
		this.x = pos.getX();
		this.y = pos.getY();
		this.z = pos.getZ();
		this.bindPointType = 4;
		this.kiskObjId = kisk.isActive() ? kisk.getObjectId() : 0;
	}

	@Override
	protected void writeImpl(AionConnection con) {
		writeC(bindPointType);
		writeC(0x01); // unk
		writeD(mapId);// map id
		writeF(x); // x
		writeF(y); // y
		writeF(z); // z
		writeD(kiskObjId); // if 0 and in type = 4 will clear the current display
	}

}
