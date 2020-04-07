package com.aionemu.gameserver.model.templates.item.actions;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.model.gameobjects.HouseObject;
import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.Persistable.PersistentState;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_HOUSE_EDIT;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.network.aion.serverpackets.SM_UPDATE_PLAYER_APPEARANCE;
import com.aionemu.gameserver.services.item.ItemPacketService;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author IceReaper, Neon
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DyeAction")
public class DyeAction extends AbstractItemAction {

	@XmlAttribute(name = "color")
	protected String color;
	@XmlAttribute
	private Integer minutes;

	@Override
	public boolean canAct(Player player, Item parentItem, Item targetItem, Object... params) {
		HouseObject<?> targetHouseObject = (HouseObject<?>) params[0];
		if (targetHouseObject == null && targetItem == null) { // nothing to dye
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_ITEM_COLOR_ERROR());
			return false;
		}
		if (targetHouseObject != null) {
			if (color.equals("no") && targetHouseObject.getColor() == null) {
				PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_ITEM_PAINT_ERROR_CANNOTREMOVE());
				return false;
			}
			if (!targetHouseObject.getObjectTemplate().getCanDye()) {
				PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_ITEM_PAINT_ERROR_CANNOTPAINT());
				return false;
			}
		}
		return true;
	}

	@Override
	public void act(Player player, Item parentItem, Item targetItem, Object... params) {
		HouseObject<?> targetHouseObject = (HouseObject<?>) params[0];
		if (targetHouseObject == null)
			dyeItem(player, parentItem, targetItem);
		else
			dyeHouseObject(player, parentItem, targetHouseObject);
	}

	private void dyeItem(Player player, Item parentItem, Item targetItem) {
		if (!targetItem.getItemSkinTemplate().isItemDyePermitted())
			return;
		if (!player.getInventory().decreaseByObjectId(parentItem.getObjectId(), 1))
			return;
		targetItem.setItemColor(getColor());
		if (minutes != null)
			targetItem.setColorExpireTime((int) (System.currentTimeMillis() / 1000 + minutes * 60));
		else
			targetItem.setColorExpireTime(0);
		if (targetItem.getItemColor() == null) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_ITEM_COLOR_REMOVE_SUCCEED(targetItem.getL10n()));
		} else {
			PacketSendUtility.sendPacket(player,
				SM_SYSTEM_MESSAGE.STR_ITEM_COLOR_CHANGE_SUCCEED(targetItem.getL10n(), parentItem.getL10n()));
		}

		// item is equipped, so need broadcast packet
		if (player.getEquipment().getEquippedItemByObjId(targetItem.getObjectId()) != null) {
			PacketSendUtility.broadcastPacket(player,
				new SM_UPDATE_PLAYER_APPEARANCE(player.getObjectId(), player.getEquipment().getEquippedForAppearance()), true);
			player.getEquipment().setPersistentState(PersistentState.UPDATE_REQUIRED);
		} else { // item is not equipped
			player.getInventory().setPersistentState(PersistentState.UPDATE_REQUIRED);
		}

		ItemPacketService.updateItemAfterInfoChange(player, targetItem);
	}

	public Integer getColor() {
		return color.equals("no") ? null : Integer.parseInt(color, 16);
	}

	private void dyeHouseObject(Player player, Item dyeItem, HouseObject<?> houseObject) {
		if (!player.getInventory().decreaseByObjectId(dyeItem.getObjectId(), 1))
			return;
		houseObject.setColor(getColor());
		float x = houseObject.getX();
		float y = houseObject.getY();
		float z = houseObject.getZ();
		int rotation = houseObject.getRotation();
		PacketSendUtility.sendPacket(player, new SM_HOUSE_EDIT(7, 0, houseObject.getObjectId()));
		PacketSendUtility.sendPacket(player, new SM_HOUSE_EDIT(5, houseObject.getObjectId(), x, y, z, rotation));
		houseObject.spawn();
		String objectName = houseObject.getObjectTemplate().getL10n();
		if (houseObject.getColor() == null) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_ITEM_PAINT_REMOVE_SUCCEED(objectName));
		} else {
			String paintName = dyeItem.getItemTemplate().getL10n();
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_ITEM_PAINT_SUCCEED(objectName, paintName));
		}
	}

}
