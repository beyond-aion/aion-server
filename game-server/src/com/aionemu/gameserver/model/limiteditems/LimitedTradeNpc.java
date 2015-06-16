package com.aionemu.gameserver.model.limiteditems;

import javolution.util.FastList;

/**
 *
 * @author xTz
 */
public class LimitedTradeNpc {

	private FastList<LimitedItem> limitedItems;

	public LimitedTradeNpc(FastList<LimitedItem> limitedItems) {
		this.limitedItems = limitedItems;
		
	}

	public void putLimitedItems(FastList<LimitedItem> limitedItems) {
		this.limitedItems.addAll(limitedItems);
	}

	public FastList<LimitedItem> getLimitedItems() {
		return limitedItems;
	}
}
