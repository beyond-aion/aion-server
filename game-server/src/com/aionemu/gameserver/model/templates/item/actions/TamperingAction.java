package com.aionemu.gameserver.model.templates.item.actions;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.configs.main.CustomConfig;
import com.aionemu.gameserver.configs.main.LoggingConfig;
import com.aionemu.gameserver.configs.main.RatesConfig;
import com.aionemu.gameserver.controllers.observer.ItemUseObserver;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.TaskId;
import com.aionemu.gameserver.model.enchants.TemperingEffect;
import com.aionemu.gameserver.model.enchants.TemperingStat;
import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.Persistable.PersistentState;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.player.Rates;
import com.aionemu.gameserver.model.templates.item.enums.ItemGroup;
import com.aionemu.gameserver.network.aion.serverpackets.SM_INVENTORY_UPDATE_ITEM;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ITEM_USAGE_ANIMATION;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.utils.collections.Predicates;

/**
 * @author Rolandas
 */
public class TamperingAction extends AbstractItemAction {

	private static final Logger log = LoggerFactory.getLogger("TAMPERING_LOG");

	@Override
	public boolean canAct(Player player, Item parentItem, Item targetItem, Object... params) {
		int maxTemp = targetItem.getItemTemplate().getMaxTampering();
		if (!(maxTemp > 0) || targetItem.getTempering() >= maxTemp) {
			return false;
		}
		return true;
	}

	@Override
	public void act(final Player player, final Item parentItem, final Item targetItem, Object... params) {
		final int parentItemId = parentItem.getItemId();
		final int parntObjectId = parentItem.getObjectId();
		PacketSendUtility.broadcastPacket(player, new SM_ITEM_USAGE_ANIMATION(player.getObjectId(), parentItem.getObjectId(), parentItemId, 5000, 0, 0),
			true);
		final ItemUseObserver observer = new ItemUseObserver() {

			@Override
			public void abort() {
				player.getController().cancelTask(TaskId.ITEM_USE);
				player.removeItemCoolDown(parentItem.getItemTemplate().getUseLimits().getDelayId());
				PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_ITEM_AUTHORIZE_CANCEL(targetItem.getL10n()));
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
					PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_ENCHANT_ITEM_NO_TARGET_ITEM());
					PacketSendUtility.broadcastPacketAndReceive(player,
						new SM_ITEM_USAGE_ANIMATION(player.getObjectId(), parntObjectId, parentItemId, 0, 2, 0));
					return;
				}

				if (!player.getInventory().decreaseByObjectId(parntObjectId, 1)) {
					PacketSendUtility.broadcastPacketAndReceive(player,
						new SM_ITEM_USAGE_ANIMATION(player.getObjectId(), parntObjectId, parentItemId, 0, 2, 0));
					return;
				}

				int maxTemp = targetItem.getItemTemplate().getMaxTampering();
				if (targetItem.getTempering() < maxTemp) {
					if (targetItem.getTemperingEffect() != null) {
						targetItem.getTemperingEffect().endEffect(player);
						targetItem.setTemperingEffect(null);
					}

					float temperingChance = calculateChance(player, targetItem);
					if (Rnd.chance() < temperingChance) {
						targetItem.setTempering(targetItem.getTempering() + 1);
						if (targetItem.getTempering() > 4 && targetItem.getItemTemplate().getItemGroup() == ItemGroup.PLUME) {
							// Random chance to get 4-7 ATK/20-32 MBoost
							if (targetItem.getItemTemplate().getTemperingName().equals("TSHIRT_PHYSICAL")) {
								targetItem.setRndPlumeBonusValue(targetItem.getRndPlumeBonusValue() + Rnd.get(0, 3));
							} else {
								targetItem.setRndPlumeBonusValue(targetItem.getRndPlumeBonusValue() + Rnd.get(0, 12));
							}
						}
						if (targetItem.isEquipped()) {
							if (targetItem.getItemTemplate().getItemGroup() == ItemGroup.PLUME) {
								targetItem.setTemperingEffect(new TemperingEffect(player, targetItem));
							} else {
								Map<Integer, List<TemperingStat>> tempering = DataManager.TEMPERING_DATA.getTemplates(targetItem.getItemTemplate());
								if (tempering != null)
									targetItem.setTemperingEffect(new TemperingEffect(player, tempering.get(targetItem.getTempering())));
							}
						}
						PacketSendUtility.sendPacket(player,
							SM_SYSTEM_MESSAGE.STR_MSG_ITEM_AUTHORIZE_SUCCEEDED(targetItem.getL10n(), targetItem.getTempering()));
						PacketSendUtility.broadcastPacketAndReceive(player,
							new SM_ITEM_USAGE_ANIMATION(player.getObjectId(), parntObjectId, parentItemId, 0, 1, 0));

						if (CustomConfig.ENABLE_ENCHANT_ANNOUNCE && targetItem.getTempering() == 10) {
							PacketSendUtility.broadcastToWorld(
								SM_SYSTEM_MESSAGE.STR_MSG_ITEM_AUTHORIZE_SUCCEEDED_MAX(player.getName(), targetItem.getItemTemplate().getL10n(),
									targetItem.getTempering()),
								Predicates.Players.sameRace(player));
						}

						if (LoggingConfig.LOG_TAMPERING)
							log.info("Player " + player.getName() + " successfully tampered item " + targetItem.getItemId() + "(" + targetItem.getObjectId()
								+ ") to level " + targetItem.getTempering());
					} else {
						targetItem.setTempering(0);
						if (targetItem.getItemTemplate().getItemGroup() == ItemGroup.PLUME) {
							PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_ITEM_AUTHORIZE_FAILED_TSHIRT(targetItem.getL10n()));
							PacketSendUtility.broadcastPacketAndReceive(player,
								new SM_ITEM_USAGE_ANIMATION(player.getObjectId(), parntObjectId, parentItemId, 0, 2, 0));
							if (targetItem.isEquipped())
								player.getEquipment().decreaseEquippedItemCount(targetItem.getObjectId(), 1);
							else
								player.getInventory().decreaseByObjectId(targetItem.getObjectId(), 1);
						} else {
							PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_ITEM_AUTHORIZE_FAILED(targetItem.getL10n()));
							PacketSendUtility.broadcastPacketAndReceive(player,
								new SM_ITEM_USAGE_ANIMATION(player.getObjectId(), parntObjectId, parentItemId, 0, 2, 0));
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

	private float calculateChance(Player player, Item item) {
		if (item.getTempering() == 0) // +0 -> +1 is always safe
			return 100;
		if (item.getItemTemplate().getItemGroup() == ItemGroup.PLUME)
			return Math.max(25, 100 - (item.getTempering() * 10));
		return Rates.get(player, RatesConfig.TEMPERING_CHANCES);
	}
}
