package com.aionemu.gameserver.network.aion.clientpackets;

import java.util.Set;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.player.ReviveType;
import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection.State;
import com.aionemu.gameserver.services.player.PlayerReviveService;

/**
 * @author ATracer, orz, avol, Simple
 */
public class CM_REVIVE extends AionClientPacket {

	private int reviveId;

	/**
	 * Constructs new instance of <tt>CM_REVIVE </tt> packet
	 * 
	 * @param opcode
	 */
	public CM_REVIVE(int opcode, Set<State> validStates) {
		super(opcode, validStates);
	}

	@Override
	protected void readImpl() {
		reviveId = readUC();
	}

	@Override
	protected void runImpl() {
		Player activePlayer = getConnection().getActivePlayer();

		if (!activePlayer.isDead())
			return;

		ReviveType reviveType = ReviveType.getReviveTypeById(reviveId);

		switch (reviveType) {
			case BIND_REVIVE:
			case OBELISK_REVIVE:
				PlayerReviveService.bindRevive(activePlayer);
				break;
			case REBIRTH_REVIVE:
				PlayerReviveService.rebirthRevive(activePlayer);
				break;
			case ITEM_SELF_REVIVE:
				PlayerReviveService.itemSelfRevive(activePlayer);
				break;
			case SKILL_REVIVE:
				PlayerReviveService.skillRevive(activePlayer);
				break;
			case KISK_REVIVE:
				PlayerReviveService.kiskRevive(activePlayer);
				break;
			case INSTANCE_REVIVE:
				PlayerReviveService.instanceRevive(activePlayer);
				break;
		}

	}
}
