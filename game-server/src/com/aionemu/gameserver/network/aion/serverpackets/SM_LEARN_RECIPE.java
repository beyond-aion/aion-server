package com.aionemu.gameserver.network.aion.serverpackets;

import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;

/**
 * @author SVDNESS
 */
public class SM_LEARN_RECIPE extends AionServerPacket {
	private final int recipeId;
	private final int productionCount;

	public SM_LEARN_RECIPE(int recipeId, int productionCount) {
		this.recipeId = recipeId;
		this.productionCount = productionCount;
	}

	@Override
	protected void writeImpl(AionConnection con) {
		writeD(recipeId);
		writeC(productionCount); // Remaining recipe production count; 0: unlimited uses.
	}
}