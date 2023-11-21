package com.aionemu.gameserver.model.limiteditems;

import java.util.ArrayList;
import java.util.List;

/**
 * @author xTz
 */
public class LimitedTradeNpc {

	private final List<LimitedItem> limitedItems = new ArrayList<>();

	public void addLimitedItems(List<LimitedItem> limitedItems) {
		this.limitedItems.addAll(limitedItems);
	}

	public List<LimitedItem> getLimitedItems() {
		return limitedItems;
	}
}
