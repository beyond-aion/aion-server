package com.aionemu.gameserver.model.templates.item.actions;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.item.ItemTemplate;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ITEM_USAGE_ANIMATION;
import com.aionemu.gameserver.network.aion.serverpackets.SM_MEGAPHONE;
import com.aionemu.gameserver.network.aion.serverpackets.SM_MEGAPHONE.FactionLabel;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.collections.Predicates;

/**
 * @author Rolandas, ginho1
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "MegaphoneAction")
public class MegaphoneAction extends AbstractItemAction {

	@XmlAttribute(name = "color")
	protected String color;

	public int getColor() {
		int rgb = Integer.parseInt(color, 16);
		return rgb;
	}

	@Override
	public boolean canAct(Player player, Item item, Item targetItem, Object... params) {
		return params.length > 0 && params[0] instanceof String; // the message only comes with CM_MEGAPHONE
	}

	@Override
	public void act(Player player, Item item, Item targetItem, Object... params) {
		if (!player.getInventory().decreaseByObjectId(item.getObjectId(), 1))
			return;
		String message = (String) params[0];
		ItemTemplate itemTemplate = item.getItemTemplate();
		PacketSendUtility.broadcastPacket(player, new SM_ITEM_USAGE_ANIMATION(player.getObjectId(), item.getObjectId(), itemTemplate.getTemplateId()),
			true);
		PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_USE_ITEM(item.getL10n()));
		if (isCrossFaction(item.getItemId()))
			PacketSendUtility.broadcastToWorld(new SM_MEGAPHONE(player, message, item.getItemId()));
		else
			PacketSendUtility.broadcastToWorld(new SM_MEGAPHONE(FactionLabel.NONE, player.getName(), message, item.getItemId()),
				Predicates.Players.sameRace(player));
	}

	/**
	 * Race megaphones (188930000+) reach both factions and show the sender's faction, the others only reach the sender's own faction.
	 */
	private static boolean isCrossFaction(int itemId) {
		return itemId / 10000 == 18893;
	}
}
