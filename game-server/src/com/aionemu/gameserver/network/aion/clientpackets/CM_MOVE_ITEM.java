package com.aionemu.gameserver.network.aion.clientpackets;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection.State;
import com.aionemu.gameserver.services.item.ItemMoveService;

/**
 * @author alexa026, kosyachok
 */
public class CM_MOVE_ITEM extends AionClientPacket {

	/**
	 * Target object id that client wants to TALK WITH or 0 if wants to unselect
	 */
	private int targetObjectId;
	private byte source;
	private byte destination;
	private short slot;

	public CM_MOVE_ITEM(int opcode, State state, State... restStates) {
		super(opcode, state, restStates);
	}

	@Override
	protected void readImpl() {
		targetObjectId = readD();// empty
		source = readSC(); // FROM (0 - player inventory, 1 - regular warehouse, 2 - account warehouse, 3 - legion warehouse)
		destination = readSC(); // TO
		slot = readSH();
	}

	@Override
	protected void runImpl() {
		Player player = getConnection().getActivePlayer();
		ItemMoveService.moveItem(player, targetObjectId, source, destination, slot);
	}
}
