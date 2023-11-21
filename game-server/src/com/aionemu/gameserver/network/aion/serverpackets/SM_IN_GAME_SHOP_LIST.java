package com.aionemu.gameserver.network.aion.serverpackets;

import java.util.*;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.ingameshop.IGItem;
import com.aionemu.gameserver.model.ingameshop.InGameShopEn;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;

/**
 * @author xTz, KID
 */
public class SM_IN_GAME_SHOP_LIST extends AionServerPacket {

	private Player player;
	private int nrList;
	private int salesRanking;

	public SM_IN_GAME_SHOP_LIST(Player player, int nrList, int salesRanking) {
		this.player = player;
		this.nrList = nrList;
		this.salesRanking = salesRanking;
	}

	@Override
	protected void writeImpl(AionConnection con) {
		Map<Integer, List<IGItem>> allItems = new HashMap<>();
		List<IGItem> inAllItems;
		Collection<IGItem> items;
		byte category = player.inGameShop.getCategory();
		byte subCategory = player.inGameShop.getSubCategory();
		if (salesRanking == 1) {
			items = InGameShopEn.getInstance().getItems(category);
			int size = 0;
			int tabSize = 9;
			int f = 0;
			for (IGItem a : items) {
				if (subCategory != 2)
					if (a.getSubCategory() != subCategory)
						continue;

				if (size == tabSize) {
					tabSize += 9;
					f++;
				}
				allItems.computeIfAbsent(f, k -> new ArrayList<>()).add(a);
				size++;
			}

			inAllItems = allItems.get(nrList);
			writeD(salesRanking);
			writeD(nrList);
			writeD(size > 0 ? tabSize : 0);
			writeH(inAllItems == null ? 0 : inAllItems.size());
			if (inAllItems != null)
				for (IGItem item : inAllItems)
					writeD(item.getObjectId());
		} else {
			List<Integer> salesRankingItems = InGameShopEn.getInstance().getTopSales(subCategory, category);
			writeD(salesRanking);
			writeD(nrList);
			writeD((InGameShopEn.getInstance().getMaxList(subCategory, category) + 1) * 9);
			writeH(salesRankingItems.size());
			for (int id : salesRankingItems)
				writeD(id);

		}
	}

}
