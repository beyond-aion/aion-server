package com.aionemu.gameserver.network.aion.clientpackets;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection.State;
import com.aionemu.gameserver.network.aion.serverpackets.SM_RECIPE_LIST;

import java.util.Set;

/**
 * @author SVDNESS
 */
public class CM_RECIPE_LIST extends AionClientPacket {

	public CM_RECIPE_LIST(int opcode, Set<State> validStates) {
		super(opcode, validStates);
	}

	@Override
	protected void readImpl() {}

	@Override
	protected void runImpl() {
		Player player = getConnection().getActivePlayer();
		if (player == null) {
			return;
		}
		sendPacket(new SM_RECIPE_LIST(player.getRecipeList().getRecipes()));
	}
}