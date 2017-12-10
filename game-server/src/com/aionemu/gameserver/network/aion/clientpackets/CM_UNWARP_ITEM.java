package com.aionemu.gameserver.network.aion.clientpackets;

import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.Persistable.PersistentState;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.serverpackets.SM_INVENTORY_UPDATE_ITEM;
import com.aionemu.gameserver.network.aion.serverpackets.SM_UNWARP_ITEM;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author xTz
 */
public class CM_UNWARP_ITEM extends AionClientPacket {

	private int objectId;

	public CM_UNWARP_ITEM(int opcode, AionConnection.State state, AionConnection.State... restStates) {
		super(opcode, state, restStates);
	}

	@Override
	protected void readImpl() {
		objectId = readD();
	}

	@Override
	protected void runImpl() {
		Player player = getConnection().getActivePlayer();
		if (player == null) {
			return;
		}
		Item item = player.getInventory().getItemByObjId(objectId);
		if (item != null) {
			if (item.getPackCount() > 0) {
				sendPacket(new SM_UNWARP_ITEM(objectId, item.getPackCount()));
				item.setPackCount(item.getPackCount() * -1);
				item.setPersistentState(PersistentState.UPDATE_REQUIRED);
				PacketSendUtility.sendPacket(player, new SM_INVENTORY_UPDATE_ITEM(player, item));
			}
		}
	}

}
