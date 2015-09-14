package com.aionemu.gameserver.model.limiteditems;

import javolution.util.FastTable;

/**
 *
 * @author xTz
 */
public class LimitedTradeNpc {

	private FastTable<LimitedItem> limitedItems;

	public LimitedTradeNpc(FastTable<LimitedItem> limitedItems) {
		this.limitedItems = limitedItems;
		
	}

	public void putLimitedItems(FastTable<LimitedItem> limitedItems) {
		this.limitedItems.addAll(limitedItems);
	}

	public FastTable<LimitedItem> getLimitedItems() {
		return limitedItems;
	}
}
