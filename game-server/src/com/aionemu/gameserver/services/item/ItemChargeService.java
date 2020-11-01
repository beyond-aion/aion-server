package com.aionemu.gameserver.services.item;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.player.RequestResponseHandler;
import com.aionemu.gameserver.model.items.ChargeInfo;
import com.aionemu.gameserver.model.templates.item.Improvement;
import com.aionemu.gameserver.network.aion.serverpackets.SM_INVENTORY_UPDATE_ITEM;
import com.aionemu.gameserver.network.aion.serverpackets.SM_QUESTION_WINDOW;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.abyss.AbyssPointsService;
import com.aionemu.gameserver.services.item.ItemPacketService.ItemUpdateType;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author ATracer
 */
public class ItemChargeService {

	public static Collection<Item> filterItemsToCondition(Player player, Item selectedItem, int chargeWay) {
		if (selectedItem != null)
			return Collections.singletonList(selectedItem);
		return player.getEquipment().getEquippedItems().stream().filter(item -> item.calculateAvailableChargeLevel(player) != 0
			&& item.getImprovement() != null && item.getImprovement().getChargeWay() == chargeWay && item.getChargePoints() < ChargeInfo.LEVEL2)
			.collect(Collectors.toList());
	}

	public static void startChargingEquippedItems(Player player, int senderObj, int chargeWay) {
		// TODO: Check this : SM_QUESTION_WINDOW.STR_ITEM_CHARGE_CONFIRM_SOME_ALREADY_CHARGED !!!
		final Collection<Item> filteredItems = filterItemsToCondition(player, null, chargeWay);
		if (filteredItems.isEmpty()) {
			if (chargeWay == 1)
				PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_ITEM_CHARGE_ALL_FAIL_NO_CHARGEABLE_EQUIPMENT());
			else
				PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_ITEM_CHARGE2_ALL_FAIL_NO_CHARGEABLE_EQUIPMENT());
			return;
		}

		final long payAmount = calculatePrice(filteredItems, player);
		RequestResponseHandler<Player> request = new RequestResponseHandler<>(player) {

			@Override
			public void acceptRequest(Player requester, Player responder) {
				if (processPayment(player, chargeWay, payAmount))
					chargeItems(player, filteredItems, 2, false, false);
			}

		};
		int msg = chargeWay == 1 ? SM_QUESTION_WINDOW.STR_ITEM_CHARGE_ALL_CONFIRM : SM_QUESTION_WINDOW.STR_ITEM_CHARGE2_ALL_CONFIRM;
		if (player.getResponseRequester().putRequest(msg, request))
			PacketSendUtility.sendPacket(player, new SM_QUESTION_WINDOW(msg, senderObj, 0, String.valueOf(payAmount)));
	}

	private static long calculatePrice(Collection<Item> items, Player player) {
		long result = 0;
		for (Item item : items)
			result += getPayAmountForService(item, item.calculateAvailableChargeLevel(player));
		return result;
	}

	public static void chargeItems(Player player, Collection<Item> items, int maxLevel, boolean ignoreRankRequirement, boolean requirePayment) {
		if (items.isEmpty())
			return;
		Set<Integer> chargeWays = new HashSet<>(2);
		boolean itemsUpdated = false;
		for (Item item : items) {
			if (chargeItem(player, item, maxLevel, ignoreRankRequirement, requirePayment)) {
				itemsUpdated = true;
				chargeWays.add(item.getImprovement().getChargeWay());
			}
		}
		if (!itemsUpdated)
			return;
		chargeWays.forEach(chargeWay -> {
			if (chargeWay == 1)
				PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_ITEM_CHARGE_ALL_COMPLETE());
			else
				PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_ITEM_CHARGE2_ALL_COMPLETE());
		});
	}

	public static boolean chargeItem(Player player, Item item, int maxLevel, boolean ignoreRankRequirement, boolean requirePayment) {
		Improvement improvement = item.getImprovement();
		if (improvement == null)
			return false;

		int level = ignoreRankRequirement ? maxLevel : calculateMaxChargeLevelBasedOnRank(player, item, maxLevel);
		if (level <= 0)
			return false;
		int maxChargePoints = level == 1 ? ChargeInfo.LEVEL1 : ChargeInfo.LEVEL2;
		int chargePointsToAdd = Math.max(0, maxChargePoints - item.getChargePoints());
		// process payment if needed
		if (chargePointsToAdd <= 0 || requirePayment && !processPayment(player, item, level))
			return false;

		if (item.getConditioningInfo().updateChargePoints(chargePointsToAdd))
			PacketSendUtility.sendPacket(player, new SM_INVENTORY_UPDATE_ITEM(player, item, ItemUpdateType.CHARGE));

		if (improvement.getChargeWay() == 1) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_ITEM_CHARGE_SUCCESS(item.getL10n(), level));
		} else {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_ITEM_CHARGE2_SUCCESS(item.getL10n(), level));
		}
		player.getGameStats().updateStatsVisually();
		return true;
	}

	public static boolean processPayment(Player player, Item item, int level) {
		return processPayment(player, item.getImprovement().getChargeWay(), getPayAmountForService(item, level));
	}

	public static boolean processPayment(Player player, int chargeWay, long amount) {
		return switch (chargeWay) {
			case 1 -> processKinahPayment(player, amount);
			case 2 -> processAPPayment(player, amount);
			default -> false;
		};
	}

	public static boolean processKinahPayment(Player player, long requiredKinah) {
		return player.getInventory().tryDecreaseKinah(requiredKinah);
	}

	public static boolean processAPPayment(Player player, long requiredAP) {
		if (player.getAbyssRank().getAp() < requiredAP)
			return false;
		AbyssPointsService.addAp(player, (int) -requiredAP);
		return true;
	}

	public static long getPayAmountForService(Item item, int chargeLevel) {
		Improvement improvement = item.getImprovement();
		if (improvement == null)
			return 0;
		int price1 = improvement.getPrice1();
		int price2 = improvement.getPrice2();
		double firstLevel = price1 / 2d;
		double updateLevel = Math.round(firstLevel + (price2 - price1) / 2d);
		double money = 0;
		float currentChargeRatio = 1f;
		switch (chargeLevel) {
			case 1:
				currentChargeRatio -= ((float) item.getChargePoints() / (float) ChargeInfo.LEVEL1);
				money = Math.ceil(firstLevel * currentChargeRatio);
				break;
			case 2:
				switch (getNextChargeLevel(item)) {
					case 1 -> {
						// full
						currentChargeRatio -= (((float) item.getChargePoints() / (float) ChargeInfo.LEVEL1));
						money = Math.ceil(firstLevel * currentChargeRatio) + updateLevel;
					}
					case 2 -> {
						// update
						currentChargeRatio -= (((float) (item.getChargePoints() - ChargeInfo.LEVEL1) / (float) (ChargeInfo.LEVEL2 - ChargeInfo.LEVEL1)));
						money = Math.ceil(updateLevel * currentChargeRatio);
					}
				}
		}
		return Math.max(0, (long) money);
	}

	private static int getNextChargeLevel(Item item) {
		int charge = item.getChargePoints();
		if (charge < ChargeInfo.LEVEL1)
			return 1;
		if (charge < ChargeInfo.LEVEL2)
			return 2;
		throw new IllegalArgumentException("Invalid charge level " + charge);
	}

	public static int calculateMaxChargeLevelBasedOnRank(Player player, Item item, int maxChargeLevel) {
		return Math.min(item.calculateAvailableChargeLevel(player), maxChargeLevel);
	}

}
