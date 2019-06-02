package com.aionemu.gameserver.network.aion.serverpackets;

import java.util.Set;

import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;

/**
 * @author lord_rex
 */
public class SM_RECIPE_LIST extends AionServerPacket {

	private final Set<Integer> recipeIds;

	public SM_RECIPE_LIST(Set<Integer> recipeIds) {
		this.recipeIds = recipeIds;
	}

	@Override
	protected void writeImpl(AionConnection con) {
		writeH(recipeIds.size());
		for (int id : recipeIds) {
			writeD(id);
			writeC(0);
		}
	}
}
