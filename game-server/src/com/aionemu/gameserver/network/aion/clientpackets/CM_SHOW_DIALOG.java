package com.aionemu.gameserver.network.aion.clientpackets;

import java.util.Set;

import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection.State;

/**
 * @author alexa026, Avol modified by ATracer
 */
public class CM_SHOW_DIALOG extends AionClientPacket {

	private int targetObjectId;

	/**
	 * Constructs new instance of <tt>CM_SHOW_DIALOG </tt> packet
	 * 
	 * @param opcode
	 */
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
		if (player.isTrading())
			return;

		VisibleObject obj = player.getKnownList().getObject(targetObjectId);

		if (obj instanceof Npc) {
			((Npc) obj).getController().onDialogRequest(player);
		}
	}
}
