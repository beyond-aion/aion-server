package com.aionemu.gameserver.network.aion.clientpackets;

import java.util.Set;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection.State;
import com.aionemu.gameserver.services.item.ItemMoveService;

/**
 * @author alexa026, kosyachok
 */
public class CM_MOVE_ITEM extends AionClientPacket {

	private int itemObjId;
	private byte source;
	private byte destination;
	private short slot;

	public CM_MOVE_ITEM(int opcode, Set<State> validStates) {
		super(opcode, validStates);
	}

	@Override
	protected void readImpl() {
		itemObjId = readD();
		source = readC(); // FROM (0 - player inventory, 1 - regular warehouse, 2 - account warehouse, 3 - legion warehouse)
		destination = readC(); // TO
		slot = readH();
	}

	@Override
	protected void runImpl() {
		Player player = getConnection().getActivePlayer();
		ItemMoveService.moveItem(player, itemObjId, source, destination, slot);
	}
}
