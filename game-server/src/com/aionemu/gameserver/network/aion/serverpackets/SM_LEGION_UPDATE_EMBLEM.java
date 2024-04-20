package com.aionemu.gameserver.network.aion.serverpackets;

import com.aionemu.gameserver.model.team.legion.LegionEmblem;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;

/**
 * @author Simple, cura, Neon
 */
public class SM_LEGION_UPDATE_EMBLEM extends AionServerPacket {

	private int legionId;
	private byte emblemId;
	private byte color_a;
	private byte color_r;
	private byte color_g;
	private byte color_b;
	private byte emblemType;

	public SM_LEGION_UPDATE_EMBLEM(int legionId, LegionEmblem emblem) {
		this.legionId = legionId;
		this.emblemId = emblem.getEmblemId();
		this.color_a = emblem.getColor_a();
		this.color_r = emblem.getColor_r();
		this.color_g = emblem.getColor_g();
		this.color_b = emblem.getColor_b();
		this.emblemType = emblem.getEmblemType().getValue();
	}

	@Override
	protected void writeImpl(AionConnection con) {
		writeD(legionId);
		writeC(emblemId);
		writeC(emblemType);
		writeC(color_a);
		writeC(color_r);
		writeC(color_g);
		writeC(color_b);
	}
}
