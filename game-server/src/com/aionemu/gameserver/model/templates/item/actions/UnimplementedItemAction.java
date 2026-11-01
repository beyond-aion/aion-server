package com.aionemu.gameserver.model.templates.item.actions;

import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * Base of item actions whose data is already in the item templates but whose game system is missing. Using such an item tells the player it
 * cannot be used and keeps it.
 */
public abstract class UnimplementedItemAction extends AbstractItemAction {

	@Override
	public boolean canAct(Player player, Item parentItem, Item targetItem, Object... params) {
		PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_ITEM_IS_NOT_USABLE());
		return false;
	}

	@Override
	public void act(Player player, Item parentItem, Item targetItem, Object... params) {
	}
}
