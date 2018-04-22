package com.aionemu.gameserver.network.aion.clientpackets;

import java.util.Set;

import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection.State;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.DuelService;

/**
 * @author xavier
 */
public class CM_DUEL_REQUEST extends AionClientPacket {

	/**
	 * Target object id that client wants to start duel with
	 */
	private int objectId;

	/**
	 * Constructs new instance of <tt>CM_DUEL_REQUEST</tt> packet
	 * 
	 * @param opcode
	 */
	public CM_DUEL_REQUEST(int opcode, Set<State> validStates) {
		super(opcode, validStates);
	}

	@Override
	protected void readImpl() {
		objectId = readD();
	}

	@Override
	protected void runImpl() {
		Player activePlayer = getConnection().getActivePlayer();
		VisibleObject target = activePlayer.getKnownList().getObject(objectId);

		if (activePlayer.isDead())
			return;

		if (target instanceof Player && !target.equals(activePlayer)) {
			DuelService.getInstance().onDuelRequest(activePlayer, (Player) target);
		} else {
			sendPacket(SM_SYSTEM_MESSAGE.STR_DUEL_PARTNER_INVALID(target.getName()));
		}
	}
}
