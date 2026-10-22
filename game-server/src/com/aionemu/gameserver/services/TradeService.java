package com.aionemu.gameserver.services;

import java.util.*;
import java.util.function.IntFunction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.configs.main.CustomConfig;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.dataholders.GoodsListData;
import com.aionemu.gameserver.dataholders.TradeListData;
import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.items.storage.Storage;
import com.aionemu.gameserver.model.limiteditems.LimitedItem;
import com.aionemu.gameserver.model.templates.goods.GoodsList;
import com.aionemu.gameserver.model.templates.item.Acquisition;
import com.aionemu.gameserver.model.templates.item.AcquisitionType;
import com.aionemu.gameserver.model.templates.item.ItemTemplate;
import com.aionemu.gameserver.model.templates.item.TradeinItem;
import com.aionemu.gameserver.model.templates.tradelist.TradeListTemplate;
import com.aionemu.gameserver.model.templates.tradelist.TradeNpcType;
import com.aionemu.gameserver.model.trade.TradeItem;
import com.aionemu.gameserver.model.trade.TradeList;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.restrictions.PlayerRestrictions;
import com.aionemu.gameserver.services.abyss.AbyssPointsService;
import com.aionemu.gameserver.services.item.ItemFactory;
import com.aionemu.gameserver.services.item.ItemPacketService.ItemAddType;
import com.aionemu.gameserver.services.item.ItemPacketService.ItemDeleteType;
import com.aionemu.gameserver.services.item.ItemPacketService.ItemUpdateType;
import com.aionemu.gameserver.services.item.ItemService;
import com.aionemu.gameserver.services.item.ItemService.ItemUpdatePredicate;
import com.aionemu.gameserver.services.player.PlayerLimitService;
import com.aionemu.gameserver.services.trade.PricesService;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.PositionUtil;
import com.aionemu.gameserver.utils.audit.AuditLogger;

/**
 * @author ATracer, Rama, Wakizashi, xTz, Neon
 */
public class TradeService {

	private static final Logger log = LoggerFactory.getLogger(TradeService.class);
	private static final TradeListData tradeListData = DataManager.TRADE_LIST_DATA;
	private static final GoodsListData goodsListData = DataManager.GOODSLIST_DATA;

	public static List<Integer> getTradeListTabIds(TradeListTemplate tradeListTemplate, Player player) {
		List<Integer> tradeTabIds = tradeListTemplate.getTradeTabIds();
		int legionLevel = player.getLegion() == null ? 0 : player.getLegion().getLegionLevel();
		List<Integer> filteredTradeTabIds = new ArrayList<>();
		for (Integer tradeTabId : tradeTabIds) {
			GoodsList goodsList = goodsListData.getGoodsListById(tradeTabId);
			if (goodsList == null || goodsList.getLegionLevel() > legionLevel)
				continue;
			filteredTradeTabIds.add(tradeTabId);
		}
		return filteredTradeTabIds;
	}

	private static boolean canBuyLimitItem(Npc npc, Player player, int itemId, long count) {
		LimitedItem item = LimitedItemTradeService.getInstance().getLimitedItem(itemId, npc.getNpcId());
		if (item != null) {
			if (item.getDefaultSellLimit() > 0 && item.getSellLimit() - count < 0)
				return false;
			if (item.getBuyLimit() > 0 && item.getBuyCount(player.getObjectId()) + count > item.getBuyLimit())
				return false;
		}
		return true;
	}

	private static boolean containsItem(TradeListTemplate tradeListTemplate, IntFunction<GoodsList> goodsListById, int itemId) {
		for (Integer tradeTabId : tradeListTemplate.getTradeTabIds())
			if (goodsListById.apply(tradeTabId).getItemIdList().contains(itemId))
				return true;
		return false;
	}

	/**
	 * General Trade with NPC method. Handles buy items for AP and/or tokens (coins etc.) and/or kinah
	 */
	public static boolean buyFromShop(Npc npc, Player player, TradeList tradeList) {
		if (!PlayerRestrictions.canTrade(player)) {
			return false;
		}
		TradeListTemplate template = tradeListData.getTradeListTemplate(npc.getNpcId());
		if (!validateBuyItems(template, tradeList, player)) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_BUY_SELL_USER_BUY_FAILED());
			return false;
		}

		Storage inventory = player.getInventory();
		int freeSlots = inventory.getFreeSlots();
		TradeNpcType npcType = template.getTradeNpcType();
		boolean useKinah = switch (npcType) {
			case ABYSS, REWARD -> false;
			default -> true;
		};

		// strange new attributes for new trader type
		int sellModifier = npcType == TradeNpcType.ABYSS_KINAH ? template.getSellPriceRate2() : template.getSellPriceRate();
		int apSellModifier = npcType == TradeNpcType.ABYSS_KINAH ? template.getApSellPriceRate2() : template.getSellPriceRate();

		// 1. If useKinah, check for required Kinah
		if (useKinah && !tradeList.calculateBuyListPrice(player, sellModifier)) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_NOT_ENOUGH_MONEY());
			return false;
		}

		// 2. check required AP + select required items
		if (!tradeList.calculateAbyssRewardBuyList(player, apSellModifier)) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_NOT_ENOUGH_ABYSSPOINT());
			return false;
		}

		// 3. check exploit
		if (tradeList.getRequiredAp() < 0) {
			AuditLogger.log(player, "possibly used packet hack: tradeList.getRequiredAp() < 0");
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_NOT_ENOUGH_ABYSSPOINT());
			return false;
		}

		// 4. check free slots
		long requiredSlots = 0;
		for (TradeItem tradeItem : tradeList.getTradeItems()) {
			long maxStackCount = tradeItem.getItemTemplate().getMaxStackCount();
			requiredSlots += (tradeItem.getCount() + maxStackCount - 1) / maxStackCount;
		}
		if (freeSlots < requiredSlots) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_FULL_INVENTORY());
			return false;
		}

		// 5. check sell limits
		Map<Integer, Long> countByItemId = new HashMap<>();
		for (TradeItem tradeItem : tradeList.getTradeItems())
			countByItemId.merge(tradeItem.getItemId(), tradeItem.getCount(), Long::sum);
		for (Map.Entry<Integer, Long> entry : countByItemId.entrySet()) {
			if (!canBuyLimitItem(npc, player, entry.getKey(), entry.getValue())) {
				PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_LIMITED_BUYING_CANT_SELECT_NO_ITEMS());
				return false;
			}
		}

		// 6. subtract all costs
		long tradeListPrice = tradeList.getRequiredKinah();
		if (tradeList.getRequiredAp() > 0)
			AbyssPointsService.addAp(player, (int) -tradeList.getRequiredAp());

		if (useKinah && tradeListPrice > 0)
			if (!inventory.tryDecreaseKinah(tradeListPrice))
				return false;

		Map<Integer, Long> requiredItems = tradeList.getRequiredItems();
		for (Integer itemId : requiredItems.keySet()) {
			if (!player.getInventory().decreaseByItemId(itemId, requiredItems.get(itemId))) {
				AuditLogger.log(player, "tried to sell item " + itemId + " for AP, which could not be removed");
				return false;
			}
		}

		// 7. finally add items and update sell limits
		for (TradeItem tradeItem : tradeList.getTradeItems()) {
			// allow inventory overflow because player can get deranked during purchase, possibly reducing the number of free inventory slots
			ItemService.addItem(player, tradeItem.getItemId(), tradeItem.getCount(), true,
				new ItemUpdatePredicate(ItemAddType.BUY, ItemUpdateType.INC_ITEM_BUY));

			LimitedItem item = LimitedItemTradeService.getInstance().getLimitedItem(tradeItem.getItemId(), npc.getNpcId());
			if (item != null) {
				if (item.getBuyLimit() > 0)
					item.setBuyCount(player.getObjectId(), item.getBuyCount(player.getObjectId()) + (int) tradeItem.getCount());
				if (item.getDefaultSellLimit() > 0)
					item.setSellLimit(item.getSellLimit() - (int) tradeItem.getCount());
			}
		}

		return true;
	}

	private static boolean validateBuyItems(TradeListTemplate template, TradeList tradeList, Player player) {
		Set<Integer> allowedItems = new HashSet<>();
		for (Integer tradeTabId : getTradeListTabIds(template, player)) {
			GoodsList goodsList = goodsListData.getGoodsListById(tradeTabId);
			if (goodsList != null && goodsList.getItemIdList() != null)
				allowedItems.addAll(goodsList.getItemIdList());
		}

		for (TradeItem tradeItem : tradeList.getTradeItems())
			if (tradeItem.getCount() < 1 || !allowedItems.contains(tradeItem.getItemId()))
				return false;

		return true;
	}

	public static boolean performSellToShop(Player player, TradeList tradeList, TradeListTemplate purchaseTemplate) {
		return performSellToShop(player, tradeList, purchaseTemplate, PricesService.getVendorSellModifier());
	}

	/**
	 * @param tradeList
	 *          entries with item object IDs, each item at most once
	 */
	public static boolean performSellToShop(Player player, TradeList tradeList, TradeListTemplate purchaseTemplate, int sellModifier) {
		if (!PlayerRestrictions.canTrade(player))
			return false;

		// validate all rows before removing anything, so a bad row can't take the items of the previous ones
		record Sale(Item item, long count, long sellReward) {}
		Storage inventory = player.getInventory();
		List<Sale> sales = new ArrayList<>();
		for (TradeItem tradeItem : tradeList.getTradeItems()) {
			long count = tradeItem.getCount();
			Item item = inventory.getItemByObjId(tradeItem.getItemId());
			if (item == null) // don't allow to sell fake items;
				return false;

			long sellReward;

			if (purchaseTemplate != null) {
				if (!containsItem(purchaseTemplate, goodsListData::getGoodsPurchaseListById, item.getItemId()))
					return false;
				sellReward = (long) (item.getItemTemplate().getPrice() * purchaseTemplate.getBuyPriceRate() / 100D);
			} else {
				if (!item.isSellable()) {
					PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_BUY_SELL_ITEM_CAN_NOT_BE_SELLED_TO_NPC(item.getL10n()));
					return false;
				}
				sellReward = PricesService.getSellReward(item.getItemTemplate().getPrice(), sellModifier);
			}

			if (count > item.getItemCount()) {
				AuditLogger.log(player, "tried to sell more items to npc than he has");
				return false;
			}
			sales.add(new Sale(item, count, sellReward));
		}

		long kinahReward = 0;
		List<Item> items = new ArrayList<>();
		for (Sale sale : sales) {
			Item item = sale.item();
			long count = PlayerLimitService.updateSellLimit(player, sale.sellReward(), sale.count());
			if (count == 0)
				break;

			long realReward = sale.sellReward() * count;
			Item repurchaseItem;
			if (item.getItemCount() == count) {
				inventory.delete(item, ItemDeleteType.SELL);
				repurchaseItem = item;
			} else {
				repurchaseItem = ItemFactory.newItem(item.getItemId(), count);
				repurchaseItem.setExpireTime(item.getExpireTime());
				inventory.decreaseItemCount(item, count);
			}

			kinahReward += realReward;
			repurchaseItem.setRepurchasePrice(realReward);
			items.add(repurchaseItem);
		}
		RepurchaseService.getInstance().addRepurchaseItems(player, items);
		inventory.increaseKinah(kinahReward, ItemUpdateType.INC_KINAH_SELL);

		return true;
	}

	public static boolean performSellForAPToShop(Player player, TradeList tradeList, TradeListTemplate purchaseTemplate) {
		if (!CustomConfig.SELLING_APITEMS_ENABLED) {
			PacketSendUtility.sendMessage(player, "This feature is disabled");
			return false;
		}

		if (!PlayerRestrictions.canTrade(player))
			return false;

		Storage inventory = player.getInventory();
		for (TradeItem tradeItem : tradeList.getTradeItems()) {
			int itemObjectId = tradeItem.getItemId();
			long count = tradeItem.getCount();
			Item item = inventory.getItemByObjId(itemObjectId);
			if (item == null)
				return false;

			Acquisition acquisition = item.getItemTemplate().getAcquisition();
			if (acquisition == null)
				return false;
			if (!containsItem(purchaseTemplate, goodsListData::getGoodsPurchaseListById, item.getItemId()))
				return false;
			if (inventory.decreaseByObjectId(itemObjectId, count)) {
				int requiredAp = acquisition.getRequiredAp();
				int apToAdd = Math.round((requiredAp * purchaseTemplate.getBuyPriceRate()) / 100F);
				AbyssPointsService.addAp(player, apToAdd * (int) count);
			}
		}
		return true;
	}

	public static boolean performBuyFromTradeInTrade(Player player, int npcObjectId, int itemId, int count, List<Integer> tradeInItemObjectIds) {
		if (!PlayerRestrictions.canTrade(player)) {
			return false;
		}
		if (player.getInventory().isFull()) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_FULL_INVENTORY());
			return false;
		}

		if (!(player.getTarget() instanceof Npc))
			return false;

		Npc npc = (Npc) player.getTarget();
		if (!npc.canTradeIn() || npc.getObjectId() != npcObjectId || PositionUtil.getDistance(npc, player) > 10)
			return false;

		TradeListTemplate tradeInList = tradeListData.getTradeInListTemplate(npc.getNpcId());
		if (!containsItem(tradeInList, goodsListData::getGoodsInListById, itemId))
			return false;

		ItemTemplate itemTemplate = DataManager.ITEM_DATA.getItemTemplate(itemId);
		if (itemTemplate.getMaxStackCount() < count)
			return false;

		List<TradeinItem> requiredTradeInItems = itemTemplate.getTradeinList().getTradeinItem();

		Set<Integer> tradeInItemIds = new HashSet<>();
		for (Integer tradeInItemObjectId : tradeInItemObjectIds) {
			Item checkItem = player.getInventory().getItemByObjId(tradeInItemObjectId);
			if (checkItem == null) {
				AuditLogger.log(player,
					"possibly used TradeIn packet hack on " + npc + ": Player does not have the submitted item with object ID " + tradeInItemObjectId);
				return false;
			}
			tradeInItemIds.add(checkItem.getItemId());
		}

		if (tradeInItemIds.size() != requiredTradeInItems.size()) {
			AuditLogger.log(player, "possibly used TradeIn packet hack on " + npc
				+ ": The tradein list count differs from the servers templates.\nRequired: " + requiredTradeInItems + "\nSubmitted:" + tradeInItemIds);
			return false;
		}

		for (TradeinItem requiredTradeInItem : requiredTradeInItems) {
			boolean validated = false;
			for (int tradeInItemId : tradeInItemIds) {
				if (requiredTradeInItem.getId() == tradeInItemId) {
					validated = true;
					break;
				}
			}
			if (!validated) {
				AuditLogger.log(player,
					"possibly used TradeIn packet hack on " + npc + ": Did not receive all required items (expected " + requiredTradeInItem.getId() + ").");
				return false;
			}
		}

		for (TradeinItem requiredTradeInItem : requiredTradeInItems) {
			if (player.getInventory().getItemCountByItemId(requiredTradeInItem.getId()) < requiredTradeInItem.getPrice() * count)
				return false;
		}

		Acquisition aquisition = itemTemplate.getAcquisition();
		if (aquisition != null && (aquisition.getType() == AcquisitionType.ABYSS || aquisition.getType() == AcquisitionType.AP)) {
			int requiredAp = (int) ((aquisition.getRequiredAp() * count * tradeInList.getSellPriceRate() / 100.0D) * PricesService.getVendorBuyModifier())
				/ 100;
			int diferenceAp = 0;
			for (TradeinItem treadInList : requiredTradeInItems) {
				ItemTemplate itemReq = DataManager.ITEM_DATA.getItemTemplate(treadInList.getId());
				if (itemReq != null) {
					diferenceAp += (int) ((itemReq.getAcquisition().getRequiredAp() * count * tradeInList.getSellPriceRate() / 100.0D)
						* PricesService.getVendorBuyModifier()) / 100;
				}
			}
			if ((requiredAp - diferenceAp) > 0) {
				if (player.getAbyssRank().getAp() < (requiredAp - diferenceAp)) {
					PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_NOT_ENOUGH_ABYSSPOINT());
					return false;
				}
				AbyssPointsService.addAp(player, -(requiredAp - diferenceAp));
			}
		}

		for (TradeinItem requiredTradeInItem : requiredTradeInItems) {
			if (!player.getInventory().decreaseByItemId(requiredTradeInItem.getId(), requiredTradeInItem.getPrice() * count))
				return false;
		}

		ItemService.addItem(player, itemId, count);
		return true;
	}
}
