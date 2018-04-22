package com.aionemu.gameserver.network.aion.clientpackets;

import java.util.Set;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection.State;
import com.aionemu.gameserver.services.item.ItemMoveService;

/**
 * @author kosyachok
 */
public class CM_REPLACE_ITEM extends AionClientPacket {

	private byte sourceStorageType;
	private int sourceItemObjId;
	private byte replaceStorageType;
	private int replaceItemObjId;

	public CM_REPLACE_ITEM(int opcode, Set<State> validStates) {
		super(opcode, validStates);
	}

	@Override
	protected void readImpl() {
		sourceStorageType = readC();
		sourceItemObjId = readD();
		replaceStorageType = readC();
		replaceItemObjId = readD();
	}

	@Override
	protected void runImpl() {
		Player player = getConnection().getActivePlayer();
		ItemMoveService.switchItemsInStorages(player, sourceStorageType, sourceItemObjId, replaceStorageType, replaceItemObjId);
	}
}
