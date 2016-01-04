package com.aionemu.gameserver.model.templates.item.actions;

import javax.xml.bind.annotation.XmlAttribute;

import com.aionemu.gameserver.model.DescriptionId;
import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.PersistentState;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_INVENTORY_UPDATE_ITEM;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ITEM_USAGE_ANIMATION;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author Rolandas
 */
public class PackAction extends AbstractItemAction {

	@XmlAttribute
	protected UseTarget target;

	@Override
	public boolean canAct(Player player, Item parentItem, Item targetItem) {
		if (targetItem == null) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_PACK_ITEM_NO_TARGET_ITEM());
			return false;
		}
		if (targetItem.getItemTemplate().getPackCount() == 0) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_PACK_ITEM_CANNOT(new DescriptionId(targetItem.getNameId())));
			return false;
		}
		if (targetItem.isEquipped()) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_PACK_ITEM_WRONG_EQUIPED());
			return false;
		}
		if (targetItem.getItemTemplate().isTradeable()) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_PACK_ITEM_WRONG_EXCHANGE());
			return false;
		}
		if (!targetItem.isSoulBound()) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_PACK_ITEM_WRONG_SEAL());
			return false;
		}
		if (targetItem.getFusionedItemId() != 0) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_PACK_ITEM_WRONG_COMPOSITION());
			return false;
		}
		if (!targetItem.isTuned()) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_PACK_ITEM_NEED_IDENTIFY());
			return false;
		}
		if (targetItem.getItemTemplate().getItemQuality() != parentItem.getItemTemplate().getItemQuality()) {
			PacketSendUtility.sendPacket(player,
				SM_SYSTEM_MESSAGE.STR_MSG_PACK_ITEM_WRONG_QUALITY(new DescriptionId(targetItem.getNameId()), new DescriptionId(parentItem.getNameId())));
			return false;
		}
		if (targetItem.getItemTemplate().getLevel() > parentItem.getItemTemplate().getLevel()) {
			PacketSendUtility.sendPacket(player,
				SM_SYSTEM_MESSAGE.STR_MSG_PACK_ITEM_WRONG_LEVEL(new DescriptionId(targetItem.getNameId()), targetItem.getItemTemplate().getLevel()));
			return false;
		}
		UseTarget type = null;
		switch (targetItem.getItemTemplate().getItemGroup()) {
			case SWORD:
			case DAGGER:
			case MACE:
			case ORB:
			case SPELLBOOK:
			case BOW:
			case GREATSWORD:
			case POLEARM:
			case STAFF:
			case HARP:
			case GUN:
			case CANNON:
			case KEYBLADE:
				type = UseTarget.WEAPON;
				break;
			case SHIELD:
			case RB_TORSO:
			case RB_PANTS:
			case RB_SHOULDER:
			case RB_GLOVES:
			case RB_SHOES:
			case CL_TORSO:
			case CL_PANTS:
			case CL_SHOULDER:
			case CL_GLOVES:
			case CL_SHOES:
			case CH_TORSO:
			case CH_PANTS:
			case CH_SHOULDER:
			case CH_GLOVES:
			case CH_SHOES:
			case LT_TORSO:
			case LT_PANTS:
			case LT_SHOULDER:
			case LT_GLOVES:
			case LT_SHOES:
			case PL_TORSO:
			case PL_PANTS:
			case PL_SHOULDER:
			case PL_GLOVES:
			case PL_SHOES:
				type = UseTarget.ARMOR;
				break;
			case NECKLACE:
			case EARRING:
			case RING:
			case BELT:
			case HEAD:
				type = UseTarget.ACCESSORY;
				break;
			default:
				return false;
		}
		int packCount = targetItem.getPackCount();
		if (packCount > 0) { // only negative unpacked
			return false;
		}
		if (packCount < 0) {
			packCount *= -1;
		}
		if (packCount >= targetItem.getItemTemplate().getPackCount() || targetItem.isEquipped()) {
			return false;
		}
		return target == type;
	}

	@Override
	public void act(Player player, Item parentItem, Item targetItem) {
		final int parentItemId = parentItem.getItemId();
		final int parentObjectId = parentItem.getObjectId();
		int packCount = targetItem.getPackCount();
		PacketSendUtility.broadcastPacket(player, new SM_ITEM_USAGE_ANIMATION(player.getObjectId(), parentObjectId, parentItemId, 0, 1, 1), true);
		if (!player.getInventory().decreaseByObjectId(parentObjectId, 1)) {
			return;
		}
		if (packCount < 0) {
			packCount *= -1;
		}
		targetItem.setPackCount(++packCount);
		targetItem.setPersistentState(PersistentState.UPDATE_REQUIRED);
		PacketSendUtility.sendPacket(player, new SM_INVENTORY_UPDATE_ITEM(player, targetItem));
	}

	public UseTarget getTarget() {
		return target;
	}
}
