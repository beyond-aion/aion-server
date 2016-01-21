package com.aionemu.gameserver.services;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javolution.util.FastSet;
import javolution.util.FastTable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.configs.main.CustomConfig;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.dataholders.GoodsListData;
import com.aionemu.gameserver.dataholders.TradeListData;
import com.aionemu.gameserver.model.DescriptionId;
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
import com.aionemu.gameserver.model.templates.tradelist.TradeListTemplate.TradeTab;
import com.aionemu.gameserver.model.templates.tradelist.TradeNpcType;
import com.aionemu.gameserver.model.trade.TradeItem;
import com.aionemu.gameserver.model.trade.TradeList;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.restrictions.RestrictionsManager;
import com.aionemu.gameserver.services.abyss.AbyssPointsService;
import com.aionemu.gameserver.services.item.ItemFactory;
import com.aionemu.gameserver.services.item.ItemPacketService.ItemAddType;
import com.aionemu.gameserver.services.item.ItemPacketService.ItemUpdateType;
import com.aionemu.gameserver.services.item.ItemService;
import com.aionemu.gameserver.services.item.ItemService.ItemUpdatePredicate;
import com.aionemu.gameserver.services.player.PlayerLimitService;
import com.aionemu.gameserver.services.trade.PricesService;
import com.aionemu.gameserver.utils.MathUtil;
import com.aionemu.gameserver.utils.OverflowException;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.SafeMath;
import com.aionemu.gameserver.utils.audit.AuditLogger;

/**
 * @author ATracer, Rama, Wakizashi, xTz
 * @modified Neon
 */
public class TradeService {

	private static final Logger log = LoggerFactory.getLogger(TradeService.class);
	private static final TradeListData tradeListData = DataManager.TRADE_LIST_DATA;
	private static final GoodsListData goodsListData = DataManager.GOODSLIST_DATA;

	private static boolean canBuyLimitItem(Npc npc, Player player, TradeItem tradeItem) {
		LimitedItem item = LimitedItemTradeService.getInstance().getLimitedItem(tradeItem.getItemId(), npc.getNpcId());
		if (item != null) {
			if (item.getDefaultSellLimit() > 0 && item.getSellLimit() - tradeItem.getCount() < 0)
				return false;
			if (item.getBuyLimit() > 0 && item.getBuyCount(player.getObjectId()) + tradeItem.getCount() > item.getBuyLimit())
				return false;
		}
		return true;
	}

	public static boolean performBuyFromShop(Npc npc, Player player, TradeList tradeList) {
		TradeNpcType npcType = tradeListData.getTradeListTemplate(npc.getNpcId()).getTradeNpcType();
		switch (npcType) {
			case NORMAL:
			case ABYSS_KINAH:
				return performBuyTransaction(npc, player, tradeList, true);// trade including kinah
			case ABYSS:
			case REWARD:
				return performBuyTransaction(npc, player, tradeList, false); // trade without kinah
			default:
				log.warn("Unhandled TradeNpcType:" + npcType.name());
		}
		return false;
	}

	/**
	 * General Trade with NPC method. Handles buy items for AP and/or tokens (coins etc.) and/or kinah
	 * 
	 * @param player
	 * @param tradeList
	 * @param useKinah
	 *          shop subtracts kinah value of items or not
	 * @return true or false
	 */
	public static boolean performBuyTransaction(Npc npc, Player player, TradeList tradeList, boolean useKinah) {
		if (!RestrictionsManager.canTrade(player)) {
			return false;
		}

		if (!validateBuyItems(npc, tradeList, player)) {
			PacketSendUtility.sendMessage(player, "Some items are not allowed to be selled from this npc");
			return false;
		}

		Storage inventory = player.getInventory();
		int freeSlots = inventory.getFreeSlots();

		// strange new attributes for new trader type
		TradeListTemplate template = tradeListData.getTradeListTemplate(npc.getNpcId());
		int sellModifier = template.getTradeNpcType().equals(TradeNpcType.ABYSS_KINAH) ? template.getSellPriceRate2() : template.getSellPriceRate();
		int apSellModifier = template.getTradeNpcType().equals(TradeNpcType.ABYSS_KINAH) ? template.getApSellPriceRate2() : template.getSellPriceRate();

		// 1. If useKinah, check for required Kinah
		if (useKinah && !tradeList.calculateBuyListPrice(player, sellModifier)) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_NOT_ENOUGH_MONEY);
			return false;
		}

		// 2. check required AP + select required items
		if (!tradeList.calculateAbyssRewardBuyList(player, apSellModifier)) {
			// You do not have enough Abyss Points.
			PacketSendUtility.sendPacket(player, new SM_SYSTEM_MESSAGE(1300927));
			return false;
		}

		// 3. check exploit
		if (tradeList.getRequiredAp() < 0) {
			AuditLogger.info(player, "Posible client hack. tradeList.getRequiredAp() < 0");
			// You do not have enough Abyss Points.
			PacketSendUtility.sendPacket(player, new SM_SYSTEM_MESSAGE(1300927));
			return false;
		}

		// 4. check free slots
		if (freeSlots < tradeList.size()) {
			// You cannot trade as your inventory is full.
			PacketSendUtility.sendPacket(player, new SM_SYSTEM_MESSAGE(1300762));
			return false;
		}

		// 5. check sell limits
		for (TradeItem tradeItem : tradeList.getTradeItems()) {
			if (!canBuyLimitItem(npc, player, tradeItem)) {
				PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_LIMITED_BUYING_CANT_SELECT_NO_ITEMS);
				return false;
			}
		}

		// 6. subtract all costs
		long tradeListPrice = tradeList.getRequiredKinah();
		if (tradeList.getRequiredAp() > 0)
			AbyssPointsService.addAp(player, -tradeList.getRequiredAp());

		if (useKinah && tradeListPrice > 0)
			if (!inventory.tryDecreaseKinah(tradeListPrice))
				return false;

		Map<Integer, Long> requiredItems = tradeList.getRequiredItems();
		for (Integer itemId : requiredItems.keySet()) {
			if (!player.getInventory().decreaseByItemId(itemId, requiredItems.get(itemId))) {
				AuditLogger.info(player, "Possible hack. Not removed items on buy in abyss shop.");
				return false;
			}
		}

		// 7. finally add items and update sell limits
		for (TradeItem tradeItem : tradeList.getTradeItems()) {
			long notAddedCount = ItemService.addItem(player, tradeItem.getItemId(), tradeItem.getCount(), new ItemUpdatePredicate(ItemAddType.BUY, ItemUpdateType.INC_ITEM_BUY));

			LimitedItem item = LimitedItemTradeService.getInstance().getLimitedItem(tradeItem.getItemId(), npc.getNpcId());
			if (item != null) {
				if (item.getBuyLimit() > 0)
					item.setBuyCount(player.getObjectId(), item.getBuyCount(player.getObjectId()) + (int) tradeItem.getCount());
				if (item.getDefaultSellLimit() > 0)
					item.setSellLimit(item.getSellLimit() - (int) tradeItem.getCount());
			}

			if (notAddedCount != 0) {
				log.error(String.format("ItemService couldn't add all items (%d/%d) on buy: %d %d", notAddedCount, tradeItem.getCount(),
					player.getObjectId(), tradeItem.getItemId()));
				return false;
			}
		}

		return true;
	}

	/**
	 * @param tradeList
	 */
	private static boolean validateBuyItems(Npc npc, TradeList tradeList, Player player) {
		TradeListTemplate tradeListTemplate = tradeListData.getTradeListTemplate(npc.getObjectTemplate().getTemplateId());

		Set<Integer> allowedItems = new HashSet<Integer>();
		for (TradeTab tradeTab : tradeListTemplate.getTradeTablist()) {
			GoodsList goodsList = goodsListData.getGoodsListById(tradeTab.getId());
			if (goodsList != null && goodsList.getItemIdList() != null) {
				allowedItems.addAll(goodsList.getItemIdList());
			}
		}

		for (TradeItem tradeItem : tradeList.getTradeItems()) {
			if (tradeItem.getCount() < 1) {
				AuditLogger.info(player, "BUY packet hack item count < 1!");
				return false;
			}
			if (!allowedItems.contains(tradeItem.getItemId()))
				return false;
		}
		return true;
	}

	/**
	 * @param player
	 * @param tradeList
	 * @return true or false
	 */
	public static boolean performSellToShop(Player player, TradeList tradeList) {
		Storage inventory = player.getInventory();
		long kinahReward = 0;
		List<Item> items = new FastTable<>();

		if (!RestrictionsManager.canTrade(player)) {
			return false;
		}

		for (TradeItem tradeItem : tradeList.getTradeItems()) {
			Item item = inventory.getItemByObjId(tradeItem.getItemId());
			// 1) don't allow to sell fake items;
			if (item == null)
				return false;

			if (!item.isSellable()) { // %0 is not an item that can be sold.
				PacketSendUtility.sendPacket(player, new SM_SYSTEM_MESSAGE(1300344, new DescriptionId(item.getNameId())));
				return false;
			}

			Item repurchaseItem = null;
			long sellReward = PricesService.getKinahForSell(item.getItemTemplate().getPrice(), player.getRace());
			long realReward = sellReward * tradeItem.getCount();
			if (!PlayerLimitService.updateSellLimit(player, realReward))
				break;

			if (item.getItemCount() - tradeItem.getCount() < 0) {
				AuditLogger.info(player, "Trade exploit, sell item count big");
				return false;
			} else if (item.getItemCount() - tradeItem.getCount() == 0) {
				inventory.delete(item); // need to be here to avoid exploit by sending packet with many
				// items with same unique ids
				repurchaseItem = item;
			} else if (item.getItemCount() - tradeItem.getCount() > 0) {
				repurchaseItem = ItemFactory.newItem(item.getItemId(), tradeItem.getCount());
				inventory.decreaseItemCount(item, tradeItem.getCount());
			} else
				return false;

			kinahReward += realReward;
			repurchaseItem.setRepurchasePrice(realReward);
			items.add(repurchaseItem);
		}
		RepurchaseService.getInstance().addRepurchaseItems(player, items);
		inventory.increaseKinah(kinahReward);

		return true;
	}

	public static boolean performBuyFromTradeInTrade(Player player, int npcObjectId, int itemId, int count, Set<Integer> tradeInItemObjectIds) {
		if (!RestrictionsManager.canTrade(player)) {
			return false;
		}
		if (player.getInventory().isFull()) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_FULL_INVENTORY);
			return false;
		}

		if (!(player.getTarget() instanceof Npc))
			return false;

		Npc npc = (Npc) player.getTarget();
		if (!npc.canTradeIn() || npc.getObjectId() != npcObjectId || MathUtil.getDistance(npc, player) > 10)
			return false;

		TradeListTemplate tradeInList = tradeListData.getTradeInListTemplate(npc.getNpcId());
		boolean valid = false;
		for (TradeTab tab : tradeInList.getTradeTablist()) {
			GoodsList goodList = goodsListData.getGoodsInListById(tab.getId());
			if (goodList.getItemIdList().contains(itemId)) {
				valid = true;
				break;
			}
		}
		if (!valid)
			return false;

		ItemTemplate itemTemplate = DataManager.ITEM_DATA.getItemTemplate(itemId);
		if (itemTemplate.getMaxStackCount() < count)
			return false;

		List<TradeinItem> requiredTradeInItems = itemTemplate.getTradeinList().getTradeinItem();

		Set<Integer> tradeInItemIds = new FastSet<>();
		for (Integer tradeInItemObjectId : tradeInItemObjectIds) {
			Item checkItem = player.getInventory().getItemByObjId(tradeInItemObjectId);
			if (checkItem == null) {
				AuditLogger.info(player, "TradeIn packet hack. Player does not have the item which the client sent to the server.");
				return false;
			}
			tradeInItemIds.add(checkItem.getItemId());
		}

		if (tradeInItemIds.size() != requiredTradeInItems.size()) {
			AuditLogger.info(player, "TradeIn packet hack. The tradein list count differs from the servers templates.");
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
				AuditLogger.info(player, "TradeIn packet hack. Did not receive all required tradein items (expected " + requiredTradeInItem.getId() + ").");
				return false;
			}
		}

		try {
			for (TradeinItem requiredTradeInItem : requiredTradeInItems) {
				if (player.getInventory().getItemCountByItemId(requiredTradeInItem.getId()) < SafeMath.multSafe(requiredTradeInItem.getPrice(), count))
					return false;
			}

			Acquisition aquisition = itemTemplate.getAcquisition();
			if (aquisition != null && (aquisition.getType() == AcquisitionType.ABYSS || aquisition.getType() == AcquisitionType.AP)) {
				int requiredAp = (int) ((aquisition.getRequiredAp() * count * tradeInList.getSellPriceRate() / 100.0D) * PricesService.getVendorBuyModifier()) / 100;
				int diferenceAp = 0;
				for (TradeinItem treadInList : requiredTradeInItems) {
					ItemTemplate itemReq = DataManager.ITEM_DATA.getItemTemplate(treadInList.getId());
					if (itemReq != null) {
						diferenceAp += (int) ((itemReq.getAcquisition().getRequiredAp() * count * tradeInList.getSellPriceRate() / 100.0D) * PricesService
							.getVendorBuyModifier()) / 100;
					}
				}
				if ((requiredAp - diferenceAp) > 0) {
					if (player.getAbyssRank().getAp() < (requiredAp - diferenceAp)) {
						PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_NOT_ENOUGH_ABYSSPOINT);
						return false;
					}
					AbyssPointsService.addAp(player, -(requiredAp - diferenceAp));
				}
			}

			for (TradeinItem requiredTradeInItem : requiredTradeInItems) {
				if (!player.getInventory().decreaseByItemId(requiredTradeInItem.getId(), SafeMath.multSafe(requiredTradeInItem.getPrice(), count)))
					return false;
			}
		} catch (OverflowException e) {
			AuditLogger.info(player, "OverflowException using tradeInTrade " + e.getMessage());
			return false;
		}

		ItemService.addItem(player, itemId, count);
		return true;
	}

	public static boolean performSellForAPToShop(Player player, TradeList tradeList, TradeListTemplate purchaseTemplate) {
		if (!CustomConfig.SELLING_APITEMS_ENABLED) {
			PacketSendUtility.sendMessage(player, "This feature is disabled");
			return false;
		}
		if (!RestrictionsManager.canTrade(player)) {
			return false;
		}
		int tradeModifier = purchaseTemplate.getBuyPriceRate();
		Storage inventory = player.getInventory();
		for (TradeItem tradeItem : tradeList.getTradeItems()) {
			int itemObjectId = tradeItem.getItemId();
			long count = tradeItem.getCount();
			Item item = inventory.getItemByObjId(itemObjectId);
			if (item == null) {
				return false;
			}
			int itemId = item.getItemId();

			boolean valid = false;
			for (TradeTab tab : purchaseTemplate.getTradeTablist()) {
				GoodsList goodList = goodsListData.getGoodsPurchaseListById(tab.getId());
				if (goodList.getItemIdList().contains(itemId)) {
					valid = true;
					break;
				}
			}
			if (!valid)
				return false;
			if (inventory.decreaseByObjectId(itemObjectId, count)) {
				int requiredAp = item.getItemTemplate().getAcquisition().getRequiredAp();
				int apToAdd = Math.round((requiredAp * tradeModifier) / 100F);
				AbyssPointsService.addAp(player, apToAdd * (int) count);
			}
		}
		return true;
	}

	public static boolean performSellToPurchaseShop(Player player, TradeList tradeList, TradeListTemplate purchaseTemplate) {
		Storage inventory = player.getInventory();
		int tradeModifier = purchaseTemplate.getBuyPriceRate();
		long kinahReward = 0;
		List<Item> items = new FastTable<>();

		if (!RestrictionsManager.canTrade(player)) {
			return false;
		}

		for (TradeItem tradeItem : tradeList.getTradeItems()) {
			Item item = inventory.getItemByObjId(tradeItem.getItemId());
			// 1) don't allow to sell fake items;
			if (item == null)
				return false;

			int itemId = item.getItemId();
			boolean valid = false;
			for (TradeTab tab : purchaseTemplate.getTradeTablist()) {
				GoodsList goodList = goodsListData.getGoodsPurchaseListById(tab.getId());
				if (goodList.getItemIdList().contains(itemId)) {
					valid = true;
					break;
				}
			}
			if (!valid)
				return false;

			Item repurchaseItem = null;
			long sellReward = (long) (item.getItemTemplate().getPrice() * tradeModifier / 100D);
			long realReward = sellReward * tradeItem.getCount();
			if (!PlayerLimitService.updateSellLimit(player, realReward))
				break;

			if (item.getItemCount() - tradeItem.getCount() < 0) {
				AuditLogger.info(player, "Trade exploit, sell item count big");
				return false;
			} else if (item.getItemCount() - tradeItem.getCount() == 0) {
				inventory.delete(item); // need to be here to avoid exploit by sending packet with many
				// items with same unique ids
				repurchaseItem = item;
			} else if (item.getItemCount() - tradeItem.getCount() > 0) {
				repurchaseItem = ItemFactory.newItem(item.getItemId(), tradeItem.getCount());
				inventory.decreaseItemCount(item, tradeItem.getCount());
			} else
				return false;

			kinahReward += realReward;
			repurchaseItem.setRepurchasePrice(realReward);
			items.add(repurchaseItem);
		}
		RepurchaseService.getInstance().addRepurchaseItems(player, items);
		inventory.increaseKinah(kinahReward);

		return true;
	}

	/**
	 * @return the tradeListData
	 */
	public static TradeListData getTradeListData() {
		return tradeListData;
	}

	/**
	 * @return the goodsListData
	 */
	public static GoodsListData getGoodsListData() {
		return goodsListData;
	}

}
