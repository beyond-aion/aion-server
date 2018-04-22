package com.aionemu.gameserver.network.aion.clientpackets;

import java.util.Set;

import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.items.storage.Storage;
import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection.State;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.item.ItemPacketService.ItemDeleteType;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author Avol
 */
public class CM_DELETE_ITEM extends AionClientPacket {

	public int itemObjectId;

	public CM_DELETE_ITEM(int opcode, Set<State> validStates) {
		super(opcode, validStates);
	}

	@Override
	protected void readImpl() {
		itemObjectId = readD();
	}

	@Override
	protected void runImpl() {

		Player player = getConnection().getActivePlayer();
		Storage inventory = player.getInventory();
		Item item = inventory.getItemByObjId(itemObjectId);

		if (item != null) {
			if (!item.getItemTemplate().isBreakable()) {
				PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_UNBREAKABLE_ITEM(item.getL10n()));
			} else {
				inventory.delete(item, ItemDeleteType.DISCARD);
			}
		}
	}
}
