package com.aionemu.gameserver.services;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.services.CronService;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.limiteditems.LimitedItem;
import com.aionemu.gameserver.model.limiteditems.LimitedTradeNpc;
import com.aionemu.gameserver.model.templates.goods.GoodsList;
import com.aionemu.gameserver.model.templates.tradelist.TradeListTemplate.TradeTab;

/**
 * TYPE_A: BuyLimit == 0 && SellLimit != 0<br>
 * TYPE_B: BuyLimit != 0 && SellLimit == 0<br>
 * TYPE_C: BuyLimit != 0 && SellLimit != 0
 * 
 * @author xTz
 */
public class LimitedItemTradeService {

	private static final Logger log = LoggerFactory.getLogger(LimitedItemTradeService.class);
	private final Map<Integer, LimitedTradeNpc> limitedTradeNpcs = new HashMap<>();

	public void start() {
		DataManager.TRADE_LIST_DATA.getTradeListTemplate().forEach((npcId, npc) -> {
			for (TradeTab list : npc.getTradeTablist()) {
				GoodsList goodsList = DataManager.GOODSLIST_DATA.getGoodsListById(list.getId());
				if (goodsList == null) {
					log.warn("No goodslist for tradelist of npc " + npcId);
					continue;
				}
				List<LimitedItem> limitedItems = goodsList.getLimitedItems();
				if (!limitedItems.isEmpty()) {
					limitedTradeNpcs.computeIfAbsent(npcId, k -> new LimitedTradeNpc()).addLimitedItems(limitedItems);
				}
			}
		});

		for (LimitedTradeNpc limitedTradeNpc : limitedTradeNpcs.values()) {
			for (final LimitedItem limitedItem : limitedTradeNpc.getLimitedItems()) {
				CronService.getInstance().schedule(limitedItem::setToDefault, limitedItem.getSalesTime());
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
