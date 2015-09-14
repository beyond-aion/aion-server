package com.aionemu.gameserver.network.aion.serverpackets;

import com.aionemu.gameserver.model.team.legion.LegionEmblemType;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;

/**
 * @author Simple modified cura
 */
public class SM_LEGION_UPDATE_EMBLEM extends AionServerPacket {

	/** Legion emblem information **/
	private int legionId;
	private int emblemId;
	private int color_r;
	private int color_g;
	private int color_b;
	private LegionEmblemType emblemType;

	/**
	 * This constructor will handle legion emblem info
	 * 
	 * @param legionId
	 * @param emblemId
	 * @param color_r
	 * @param color_g
	 * @param color_b
	 * @param emblemType
	 */
	public SM_LEGION_UPDATE_EMBLEM(int legionId, int emblemId, int color_r, int color_g, int color_b, LegionEmblemType emblemType) {
		this.legionId = legionId;
		this.emblemId = emblemId;
		this.color_r = color_r;
		this.color_g = color_g;
		this.color_b = color_b;
		this.emblemType = emblemType;
	}

	@Override
	protected void writeImpl(AionConnection con) {
		writeD(legionId);
		writeC(emblemId);
		writeC(emblemType.getValue());
		writeC(0xFF); // Fixed
		writeC(color_r);
		writeC(color_g);
		writeC(color_b);
	}
}
