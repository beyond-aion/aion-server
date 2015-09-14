package com.aionemu.gameserver.network.aion.serverpackets;

import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;

/**
 * @author namedrisk
 */
public class SM_RECIPE_DELETE extends AionServerPacket {

	private int recipeId;

	public SM_RECIPE_DELETE(int recipeId) {
		this.recipeId = recipeId;
	}

	@Override
	protected void writeImpl(AionConnection con) {
		writeD(recipeId);
	}
}
