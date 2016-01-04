package com.aionemu.gameserver.model.templates.item.actions;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.configs.main.CustomConfig;
import com.aionemu.gameserver.configs.main.EnchantsConfig;
import com.aionemu.gameserver.configs.main.LoggingConfig;
import com.aionemu.gameserver.controllers.observer.ItemUseObserver;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.DescriptionId;
import com.aionemu.gameserver.model.TaskId;
import com.aionemu.gameserver.model.enchants.TemperingEffect;
import com.aionemu.gameserver.model.enchants.TemperingStat;
import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.PersistentState;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.item.enums.ItemGroup;
import com.aionemu.gameserver.network.aion.serverpackets.SM_INVENTORY_UPDATE_ITEM;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ITEM_USAGE_ANIMATION;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.world.World;

/**
 * @author Rolandas
 */
public class TamperingAction extends AbstractItemAction {

	private static final Logger log = LoggerFactory.getLogger("TAMPERING_LOG");

	@Override
	public boolean canAct(Player player, Item parentItem, Item targetItem) {
		int maxTemp = targetItem.getItemTemplate().getMaxTampering();
		if (!(maxTemp > 0) || targetItem.getTempering() >= maxTemp) {
			return false;
		}
		if (EnchantsConfig.MAX_TAMPERING_LEVEL > 0 && targetItem.getTempering() >= EnchantsConfig.MAX_TAMPERING_LEVEL) {
			PacketSendUtility.sendMessage(player, "You've reached max tampering level:" + EnchantsConfig.MAX_TAMPERING_LEVEL);
			return false;
		}
		return true;
	}

	@Override
	public void act(final Player player, final Item parentItem, final Item targetItem) {
		final int parentItemId = parentItem.getItemId();
		final int parntObjectId = parentItem.getObjectId();
		PacketSendUtility.broadcastPacket(player, new SM_ITEM_USAGE_ANIMATION(player.getObjectId(), parentItem.getObjectId(), parentItemId, 5000, 0, 0),
			true);
		final ItemUseObserver observer = new ItemUseObserver() {

			@Override
			public void abort() {
				player.getController().cancelTask(TaskId.ITEM_USE);
				player.removeItemCoolDown(parentItem.getItemTemplate().getUseLimits().getDelayId());
				PacketSendUtility.sendPacket(player, new SM_SYSTEM_MESSAGE(1402147, new DescriptionId(targetItem.getNameId())));
				PacketSendUtility.broadcastPacket(player, new SM_ITEM_USAGE_ANIMATION(player.getObjectId(), parntObjectId, parentItemId, 0, 3, 0), true);
				player.getObserveController().removeObserver(this);
			}
		};
		player.getObserveController().attach(observer);
		player.getController().addTask(TaskId.ITEM_USE, ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				player.getObserveController().removeObserver(observer);

				if (player.getInventory().getItemByObjId(targetItem.getObjectId()) == null && !targetItem.isEquipped()) {
					PacketSendUtility.sendPacket(player, new SM_SYSTEM_MESSAGE(1300452));
					PacketSendUtility.broadcastPacketAndReceive(player, new SM_ITEM_USAGE_ANIMATION(player.getObjectId(), parntObjectId, parentItemId, 0, 2, 0));
					return;
				}

				if (!player.getInventory().decreaseByObjectId(parntObjectId, 1)) {
					PacketSendUtility
						.broadcastPacketAndReceive(player, new SM_ITEM_USAGE_ANIMATION(player.getObjectId(), parntObjectId, parentItemId, 0, 2, 0));
					return;
				}

				int maxTemp = targetItem.getItemTemplate().getMaxTampering();
				if (targetItem.getTempering() < maxTemp) {
					if (targetItem.getTemperingEffect() != null) {
						targetItem.getTemperingEffect().endEffect(player);
						targetItem.setTemperingEffect(null);
					}

					double temperingChance = calculateChance(targetItem);
					if (Rnd.get(1, 100) <= temperingChance) {
						targetItem.setTempering(targetItem.getTempering() + 1);
						if (targetItem.isEquipped()) {
							if (targetItem.getItemTemplate().getItemGroup() == ItemGroup.PLUME) {
								targetItem.setTemperingEffect(new TemperingEffect(player, targetItem));
							} else {
								HashMap<Integer, List<TemperingStat>> tempering = DataManager.TEMPERING_DATA
									.getTemplates(targetItem.getItemTemplate().getItemGroup());
								if (tempering != null) {
									targetItem.setTemperingEffect(new TemperingEffect(player, tempering.get(targetItem.getTempering())));
								}
							}
						}
						PacketSendUtility
							.sendPacket(player, new SM_SYSTEM_MESSAGE(1402148, new DescriptionId(targetItem.getNameId()), targetItem.getTempering()));
						PacketSendUtility.broadcastPacketAndReceive(player, new SM_ITEM_USAGE_ANIMATION(player.getObjectId(), parntObjectId, parentItemId, 0, 1,
							0));

						if (CustomConfig.ENABLE_ENCHANT_ANNOUNCE && targetItem.getTempering() == 10) {
							Iterator<Player> iter = World.getInstance().getPlayersIterator();
							while (iter.hasNext()) {
								Player player2 = iter.next();
								if (player2.getRace() == player.getRace()) {
									PacketSendUtility.sendPacket(player2,
										SM_SYSTEM_MESSAGE.STR_MSG_ITEM_AUTHORIZE_SUCCEEDED_MAX(player.getName(), targetItem.getItemTemplate().getNameId()));
								}
							}
						}

						if (LoggingConfig.LOG_TAMPERING)
							log.info("Player " + player.getName() + " successfully tampered item " + targetItem.getItemId() + "(" + targetItem.getObjectId()
								+ ") to level " + targetItem.getTempering());
					} else {
						targetItem.setTempering(0);
						if (targetItem.getItemTemplate().getItemGroup() == ItemGroup.PLUME) {
							PacketSendUtility.sendPacket(player, new SM_SYSTEM_MESSAGE(1402447, new DescriptionId(targetItem.getNameId())));
							PacketSendUtility.broadcastPacketAndReceive(player, new SM_ITEM_USAGE_ANIMATION(player.getObjectId(), parntObjectId, parentItemId, 0,
								2, 0));
							if (targetItem.isEquipped())
								player.getEquipment().decreaseEquippedItemCount(targetItem.getObjectId(), 1);
							else
								player.getInventory().decreaseByObjectId(targetItem.getObjectId(), 1);
						} else {
							PacketSendUtility.sendPacket(player, new SM_SYSTEM_MESSAGE(1402149, new DescriptionId(targetItem.getNameId())));
							PacketSendUtility.broadcastPacketAndReceive(player, new SM_ITEM_USAGE_ANIMATION(player.getObjectId(), parntObjectId, parentItemId, 0,
								2, 0));
						}

						if (LoggingConfig.LOG_TAMPERING)
							log.info("Player " + player.getName() + " failed to tamper item " + targetItem.getItemId() + "(" + targetItem.getObjectId() + ").");
					}
					if (targetItem.getPersistentState() != PersistentState.DELETED) {
						targetItem.setPersistentState(PersistentState.UPDATE_REQUIRED);

						if (targetItem.isEquipped())
							player.getEquipment().setPersistentState(PersistentState.UPDATE_REQUIRED);
						else
							player.getInventory().setPersistentState(PersistentState.UPDATE_REQUIRED);

						PacketSendUtility.sendPacket(player, new SM_INVENTORY_UPDATE_ITEM(player, targetItem));
					}
				}
			}

		}, 5000));
	}

	private double calculateChance(Item target) {
		double chance = EnchantsConfig.TEMPERING_CHANCE;
		double curTemp = target.getTempering();
		if (target.getItemTemplate().getItemGroup().equals(ItemGroup.PLUME)) {
			if (curTemp < 10)
				chance = 100 - (curTemp * 10);
			else 
				chance = 10;
		}
		return chance;
	}
}
