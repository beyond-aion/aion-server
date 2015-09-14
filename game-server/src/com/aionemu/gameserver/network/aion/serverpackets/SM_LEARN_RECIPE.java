package com.aionemu.gameserver.network.aion.serverpackets;

import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;

/**
 * @author lord_rex
 */
public class SM_LEARN_RECIPE extends AionServerPacket {

	private int recipeId;

	public SM_LEARN_RECIPE(int recipeId) {
		this.recipeId = recipeId;
	}

	@Override
	protected void writeImpl(AionConnection con) {
		writeD(recipeId);
		writeC(0); // 4.0
	}
}
