package com.aionemu.gameserver.services;

import java.util.concurrent.ConcurrentHashMap;

import javolution.util.FastTable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.services.CronService;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.dataholders.GoodsListData;
import com.aionemu.gameserver.dataholders.TradeListData;
import com.aionemu.gameserver.model.limiteditems.LimitedItem;
import com.aionemu.gameserver.model.limiteditems.LimitedTradeNpc;
import com.aionemu.gameserver.model.templates.goods.GoodsList;
import com.aionemu.gameserver.model.templates.tradelist.TradeListTemplate.TradeTab;

/**
 * @author xTz
 * 
 * TYPE_A: BuyLimit == 0 && SellLimit != 0
 * TYPE_B: BuyLimit != 0 && SellLimit == 0
 * TYPE_C: BuyLimit != 0 && SellLimit != 0
 */
public class LimitedItemTradeService {

	private static final Logger log = LoggerFactory.getLogger(LimitedItemTradeService.class);
	private GoodsListData goodsListData = DataManager.GOODSLIST_DATA;
	private TradeListData tradeListData = DataManager.TRADE_LIST_DATA;
	private ConcurrentHashMap<Integer, LimitedTradeNpc> limitedTradeNpcs = new ConcurrentHashMap<Integer, LimitedTradeNpc>();

	public void start() {
		for (int npcId : tradeListData.getTradeListTemplate().keys()) {
			for (TradeTab list : tradeListData.getTradeListTemplate(npcId).getTradeTablist()) {
				GoodsList goodsList = goodsListData.getGoodsListById(list.getId());
				if (goodsList == null) {
					log.warn("No goodslist for tradelist of npc " + npcId);
					continue;
				}
				FastTable<LimitedItem> limitedItems = goodsList.getLimitedItems();
				if (limitedItems.isEmpty()) {
					continue;
				}
				if (!limitedTradeNpcs.containsKey(npcId)) {
					limitedTradeNpcs.putIfAbsent(npcId, new LimitedTradeNpc(limitedItems));
				}
				else {
					limitedTradeNpcs.get(npcId).putLimitedItems(limitedItems);
				}
			}
		}

		for (LimitedTradeNpc limitedTradeNpc : limitedTradeNpcs.values()) {
			for (final LimitedItem limitedItem : limitedTradeNpc.getLimitedItems()) {
				CronService.getInstance().schedule(new Runnable() {

					@Override
					public void run() {
						limitedItem.setToDefault();
					}

				}, limitedItem.getSalesTime());
			}
		}
		log.info("Scheduled Limited Items based on cron expression size: " + limitedTradeNpcs.size());
	}

	public LimitedItem getLimitedItem(int itemId, int npcId) {
		if (limitedTradeNpcs.containsKey(npcId)) {
			for (LimitedItem limitedItem : limitedTradeNpcs.get(npcId).getLimitedItems()) {
				if (limitedItem.getItemId() == itemId) {
					return limitedItem;
				}
			}
		}
		return null;
	}

	public boolean isLimitedTradeNpc(int npcId) {
		return limitedTradeNpcs.containsKey(npcId);
	}

	public LimitedTradeNpc getLimitedTradeNpc(int npcId) {
		return limitedTradeNpcs.get(npcId);
	}

	public static LimitedItemTradeService getInstance() {
		return SingletonHolder.INSTANCE;
	}

	private static class SingletonHolder {
		protected static final LimitedItemTradeService INSTANCE = new LimitedItemTradeService();
	}

}
