package com.aionemu.gameserver.model.trade;

import java.util.ArrayList;
import java.util.List;

import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.services.RepurchaseService;

/**
 * @author xTz
 */
public class RepurchaseList {

	private final int sellerObjId;
	private List<Item> repurchases = new ArrayList<Item>();

	public RepurchaseList(int sellerObjId) {
		this.sellerObjId = sellerObjId;
	}

	/**
	 * @param player
	 * @param itemObjectId
	 * @param count
	 */
	public void addRepurchaseItem(Player player, int itemObjectId, long count) {
		Item item = RepurchaseService.getInstance().getRepurchaseItem(player, itemObjectId);
		if (item != null) {
			repurchases.add(item);
		}
	}

	/**
	 * @return the tradeItems
	 */
	public List<Item> getRepurchaseItems() {
		return repurchases;
	}

	public int size() {
		return repurchases.size();
	}

	public final int getSellerObjId() {
		return sellerObjId;
	}
}
