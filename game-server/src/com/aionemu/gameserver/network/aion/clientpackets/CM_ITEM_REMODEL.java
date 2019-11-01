package com.aionemu.gameserver.network.aion.clientpackets;

import java.util.Set;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection.State;
import com.aionemu.gameserver.services.item.ItemRemodelService;

/**
 * @author Sarynth
 */
public class CM_ITEM_REMODEL extends AionClientPacket {

	private int keepItemId;
	private int extractItemId;

	public CM_ITEM_REMODEL(int opcode, Set<State> validStates) {
		super(opcode, validStates);
	}

	@Override
	protected void readImpl() {
		readD(); // npcId
		keepItemId = readD();
		extractItemId = readD();
		readD(); // unk 0
	}

	@Override
	protected void runImpl() {
		Player activePlayer = getConnection().getActivePlayer();
		ItemRemodelService.remodelItem(activePlayer, keepItemId, extractItemId);
	}
}
