package com.aionemu.gameserver.network.aion.serverpackets;

import java.util.Map;
import java.util.Set;

import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;

/**
 * @author SVDNESS
 */
public class SM_RECIPE_LIST extends AionServerPacket {
	private final Map<Integer, Integer> recipes;

	public SM_RECIPE_LIST(Map<Integer, Integer> recipes) {
		this.recipes = recipes;
	}

	@Override
	protected void writeImpl(AionConnection con) {
		writeH(recipes.size());
		for (Map.Entry<Integer, Integer> recipe : recipes.entrySet()) {
			writeD(recipe.getKey());
			writeC(recipe.getValue()); // Remaining recipe production count; 0: unlimited uses.
		}
	}
}