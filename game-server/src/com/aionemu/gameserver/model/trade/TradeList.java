package com.aionemu.gameserver.model.trade;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.item.Acquisition;
import com.aionemu.gameserver.model.templates.item.AcquisitionType;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.trade.PricesService;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author ATracer, Wakizashi, Neon
 */
public class TradeList {

	private int sellerObjId;

	private List<TradeItem> tradeItems = new ArrayList<>();

	private long requiredKinah;

	private int requiredAp;

	private Map<Integer, Long> requiredItems = new LinkedHashMap<>();

	public TradeList() {
	}

	public TradeList(int sellerObjId) {
		this.sellerObjId = sellerObjId;
	}

	public void addItem(int itemId, long count) {
		addTradeItem(new TradeItem(itemId, count));
	}

	public void addTradeItem(TradeItem tradeItem) {
		tradeItems.add(tradeItem);
	}

	/**
	 * @return price TradeList sum price
	 */
	public boolean calculateBuyListPrice(Player player, int modifier) {
		long availableKinah = player.getInventory().getKinah();
		requiredKinah = 0;

		for (TradeItem tradeItem : tradeItems) {
			requiredKinah += PricesService.getBuyPrice(tradeItem.getItemTemplate().getPrice(), player.getRace()) * tradeItem.getCount() * modifier / 100;
		}

		return availableKinah >= requiredKinah;
	}

	public boolean calculateAbyssRewardBuyList(Player player, int modifier) {
		int ap = player.getAbyssRank().getAp();

		this.requiredAp = 0;
		this.requiredItems.clear();

		for (TradeItem tradeItem : tradeItems) {
			Acquisition aquisition = tradeItem.getItemTemplate().getAcquisition();
			if (aquisition == null)
				continue;

			if (aquisition.getType().equals(AcquisitionType.AP) || aquisition.getType().equals(AcquisitionType.ABYSS))
				requiredAp += (int) ((aquisition.getRequiredAp() * tradeItem.getCount() * modifier / 100.0D) * PricesService.getVendorBuyModifier()) / 100;

			int rewardItemId = aquisition.getItemId();
			if (rewardItemId == 0) // no required item (medals, etc))
				continue;

			long alreadyAddedCount = 0;
			if (requiredItems.containsKey(rewardItemId))
				alreadyAddedCount = requiredItems.get(rewardItemId);
			if (alreadyAddedCount == 0)
				requiredItems.put(rewardItemId, aquisition.getItemCount() * tradeItem.getCount());
			else
				requiredItems.put(rewardItemId, alreadyAddedCount + aquisition.getItemCount() * tradeItem.getCount());
		}

		if (ap < requiredAp) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_NOT_ENOUGH_ABYSSPOINT());
			return false;
		}

		for (Integer itemId : requiredItems.keySet()) {
			long count = player.getInventory().getItemCountByItemId(itemId);
			if (requiredItems.get(itemId) < 1 || count < requiredItems.get(itemId))
				return false;
		}

		return true;
	}

	public List<TradeItem> getTradeItems() {
		return tradeItems;
	}

	public int size() {
		return tradeItems.size();
	}

	public int getSellerObjId() {
		return sellerObjId;
	}

	public int getRequiredAp() {
		return requiredAp;
	}

	public long getRequiredKinah() {
		return requiredKinah;
	}

	public Map<Integer, Long> getRequiredItems() {
		return requiredItems;
	}
}
