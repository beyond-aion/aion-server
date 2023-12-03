package com.aionemu.gameserver.network.aion.clientpackets;

import java.util.Set;

import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection.State;

/**
 * @author alexa026, Avol, ATracer
 */
public class CM_SHOW_DIALOG extends AionClientPacket {

	private int targetObjectId;

	public CM_SHOW_DIALOG(int opcode, Set<State> validStates) {
		super(opcode, validStates);
	}

	@Override
	protected void readImpl() {
		targetObjectId = readD();
	}

	@Override
	protected void runImpl() {
		Player player = getConnection().getActivePlayer();
		if (player.isProtectionActive())
			player.getController().stopProtectionActiveTask();

		if (player.isTrading())
			return;

		if (player.getKnownList().getObject(targetObjectId) instanceof Npc target) {
			target.getController().onDialogRequest(player);
		}
	}
}
