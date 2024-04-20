package com.aionemu.gameserver.network.aion.serverpackets;

import com.aionemu.gameserver.model.team.legion.LegionEmblem;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;

/**
 * @author Simple, cura, Neon
 */
public class SM_LEGION_SEND_EMBLEM extends AionServerPacket {

	private int legionId;
	private int emblemId;
	private byte emblemType;
	private int emblemDataSize; // used when EMBLEM_DATA is sent afterwards, so the client knows the incoming byte length
	private byte color_a;
	private byte color_r;
	private byte color_g;
	private byte color_b;
	private String legionName;

	public SM_LEGION_SEND_EMBLEM(int legionId, LegionEmblem emblem, int emblemDataSize, String legionName) {
		this.legionId = legionId;
		this.emblemId = emblem.getEmblemId();
		this.emblemType = emblem.getEmblemType().getValue();
		this.emblemDataSize = emblemDataSize;
		this.color_a = emblem.getColor_a();
		this.color_r = emblem.getColor_r();
		this.color_g = emblem.getColor_g();
		this.color_b = emblem.getColor_b();
		this.legionName = legionName;
	}

	@Override
	protected void writeImpl(AionConnection con) {
		writeD(legionId);
		writeC(emblemId);
		writeC(emblemType);
		writeD(emblemDataSize);
		writeC(color_a);
		writeC(color_r);
		writeC(color_g);
		writeC(color_b);
		writeS(legionName);
		writeC(0x01);
	}
}
