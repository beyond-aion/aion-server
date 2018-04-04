package com.aionemu.gameserver.model.trade;

import java.util.LinkedHashSet;
import java.util.Set;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.services.RepurchaseService;

/**
 * @author xTz
 */
public class RepurchaseList {

	private final int sellerObjId;
	private Set<Integer> repurchases = new LinkedHashSet<>();

	public RepurchaseList(int sellerObjId) {
		this.sellerObjId = sellerObjId;
	}

	public void addRepurchaseItem(Player player, int itemObjectId, long count) {
		if (RepurchaseService.getInstance().canRepurchase(player, itemObjectId))
			repurchases.add(itemObjectId);
	}

	public Set<Integer> getRepurchaseItems() {
		return repurchases;
	}

	public int size() {
		return repurchases.size();
	}

	public final int getSellerObjId() {
		return sellerObjId;
	}
}
