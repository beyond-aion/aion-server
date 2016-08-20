package com.aionemu.gameserver.network.aion.clientpackets;

import com.aionemu.gameserver.configs.main.CustomConfig;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.gameobjects.player.DeniedStatus;
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
	public CM_DUEL_REQUEST(int opcode, State state, State... restStates) {
		super(opcode, state, restStates);
	}

	@Override
	protected void readImpl() {
		objectId = readD();
	}

	@Override
	protected void runImpl() {
		Player activePlayer = getConnection().getActivePlayer();
		VisibleObject target = activePlayer.getKnownList().getObject(objectId);

		if (activePlayer.getLifeStats().isAlreadyDead())
			return;

		if (activePlayer.isInInstance() && !CustomConfig.INSTANCE_DUEL_ENABLE)
			return;

		if (target == null)
			return;

		if (target instanceof Player && !((Player) target).equals(activePlayer)) {
			DuelService duelService = DuelService.getInstance();

			Player targetPlayer = (Player) target;

			if (duelService.isDueling(activePlayer.getObjectId())) {
				sendPacket(SM_SYSTEM_MESSAGE.STR_DUEL_YOU_ARE_IN_DUEL_ALREADY());
				return;
			}
			if (duelService.isDueling(targetPlayer.getObjectId())) {
				sendPacket(SM_SYSTEM_MESSAGE.STR_DUEL_PARTNER_IN_DUEL_ALREADY(target.getName()));
				return;
			}
			if (targetPlayer.getPlayerSettings().isInDeniedStatus(DeniedStatus.DUEL)) {
				sendPacket(SM_SYSTEM_MESSAGE.STR_MSG_REJECTED_DUEL(targetPlayer.getName()));
				return;
			}
			if (targetPlayer.getLifeStats().isAlreadyDead()) {
				sendPacket(SM_SYSTEM_MESSAGE.STR_DUEL_PARTNER_INVALID(target.getName()));
				return;
			}
			duelService.onDuelRequest(activePlayer, targetPlayer);
			duelService.confirmDuelWith(activePlayer, targetPlayer);
		} else {
			sendPacket(SM_SYSTEM_MESSAGE.STR_DUEL_PARTNER_INVALID(target.getName()));
		}
	}
}
