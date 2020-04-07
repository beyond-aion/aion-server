package com.aionemu.gameserver.model.templates.item.actions;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ITEM_USAGE_ANIMATION;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author Tiger
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "FireworksUseAction")
public class FireworksUseAction extends AbstractItemAction {

	@Override
	public boolean canAct(Player player, Item parentItem, Item targetItem, Object... params) {
		return true;
	}

	@Override
	public void act(Player player, Item parentItem, Item targetItem, Object... params) {
		if (parentItem.getActivationCount() > 1)
			parentItem.setActivationCount(parentItem.getActivationCount() - 1);
		else
			player.getInventory().decreaseByObjectId(parentItem.getObjectId(), 1);

		PacketSendUtility.broadcastPacket(player, new SM_ITEM_USAGE_ANIMATION(player.getObjectId(), parentItem.getObjectId(), parentItem
			.getItemTemplate().getTemplateId(), 0, 1, 0), true);
	}
}
