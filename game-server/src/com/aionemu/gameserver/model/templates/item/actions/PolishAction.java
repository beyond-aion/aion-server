package com.aionemu.gameserver.model.templates.item.actions;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.commons.database.dao.DAOManager;
import com.aionemu.gameserver.controllers.observer.ItemUseObserver;
import com.aionemu.gameserver.dao.ItemStoneListDAO;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.TaskId;
import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.Persistable.PersistentState;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.items.IdianStone;
import com.aionemu.gameserver.model.items.RandomBonusResult;
import com.aionemu.gameserver.model.templates.item.bonuses.StatBonusType;
import com.aionemu.gameserver.network.aion.serverpackets.SM_INVENTORY_UPDATE_ITEM;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ITEM_USAGE_ANIMATION;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;

/**
 * @author Rolandas
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PolishAction")
public class PolishAction extends AbstractItemAction {

	@XmlAttribute(name = "set_id")
	protected int polishSetId;

	@Override
	public boolean canAct(Player player, Item parentItem, Item targetItem) {
		if (parentItem.getItemTemplate().getLevel() > targetItem.getItemTemplate().getLevel()) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_POLISH_WRONG_LEVEL());
			return false;
		}
		if (!targetItem.isIdentified()) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_POLISH_NEED_IDENTIFY());
			return false;
		}
		return !player.isInAttackMode() && targetItem.getItemTemplate().isWeapon() && targetItem.getItemTemplate().isCanPolish();
	}

	@Override
	public void act(final Player player, final Item parentItem, final Item targetItem) {
		final int parentItemId = parentItem.getItemId();
		final int parntObjectId = parentItem.getObjectId();
		String parentItemL10n = parentItem.getL10n();
		PacketSendUtility.broadcastPacket(player, new SM_ITEM_USAGE_ANIMATION(player.getObjectId(), parentItem.getObjectId(), parentItemId, 5000, 0, 0),
			true);
		final ItemUseObserver observer = new ItemUseObserver() {

			@Override
			public void abort() {
				player.getController().cancelTask(TaskId.ITEM_USE);
				player.removeItemCoolDown(parentItem.getItemTemplate().getUseLimits().getDelayId());
				PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_ITEM_CANCELED());
				PacketSendUtility.broadcastPacket(player, new SM_ITEM_USAGE_ANIMATION(player.getObjectId(), parntObjectId, parentItemId, 0, 2, 0), true);
				player.getObserveController().removeObserver(this);
			}

		};
		player.getObserveController().attach(observer);
		player.getController().addTask(TaskId.ITEM_USE, ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				player.getObserveController().removeObserver(observer);

				PacketSendUtility.broadcastPacket(player, new SM_ITEM_USAGE_ANIMATION(player.getObjectId(), parntObjectId, parentItemId, 0, 1, 1), true);
				if (!player.getInventory().decreaseByObjectId(parntObjectId, 1)) {
					return;
				}
				RandomBonusResult bonus = DataManager.ITEM_RANDOM_BONUSES.getRandomModifiers(StatBonusType.POLISH, polishSetId);
				if (bonus == null) {
					PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_ENCHANT_ITEM_FAILED(parentItemL10n));
					return;
				}
				PacketSendUtility.sendPacket(player, new SM_SYSTEM_MESSAGE(1401650, "[item_ex:" + targetItem.getItemId() + ";" + targetItem.getItemName()
					+ "]"));
				IdianStone idianStone = targetItem.getIdianStone();
				if (idianStone != null) {
					idianStone.onUnEquip(player);
					targetItem.setIdianStone(null);
					idianStone.setPersistentState(PersistentState.DELETED);
					DAOManager.getDAO(ItemStoneListDAO.class).storeIdianStones(idianStone);
				}
				idianStone = new IdianStone(parentItemId, PersistentState.NEW, targetItem, bonus.getTemplateNumber(), 1000000);
				targetItem.setIdianStone(idianStone);
				if (targetItem.isEquipped()) {
					idianStone.onEquip(player, targetItem.getEquipmentSlot());
				}
				PacketSendUtility.sendPacket(player, new SM_INVENTORY_UPDATE_ITEM(player, targetItem));
			}

		}, 5000));

	}

	public int getPolishSetId() {
		return polishSetId;
	}

}
