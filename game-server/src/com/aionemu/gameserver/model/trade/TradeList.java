package com.aionemu.gameserver.model.trade;

import java.util.List;
import java.util.Map;

import javolution.util.FastMap;
import javolution.util.FastTable;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.item.Acquisition;
import com.aionemu.gameserver.model.templates.item.AcquisitionType;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.trade.PricesService;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author ATracer
 * @modified Wakizashi, Neon
 */
public class TradeList {

	private int sellerObjId;

	private List<TradeItem> tradeItems = new FastTable<>();

	private long requiredKinah;

	private int requiredAp;

	private Map<Integer, Long> requiredItems = new FastMap<>();

	public TradeList() {

	}

	public TradeList(int sellerObjId) {
		this.sellerObjId = sellerObjId;
	}

	/**
	 * @param itemId
	 * @param count
	 */
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
			requiredKinah += PricesService.getKinahForBuy(tradeItem.getItemTemplate().getPrice(), player.getRace()) * tradeItem.getCount() * modifier / 100;
		}

		return availableKinah >= requiredKinah;
	}

	/**
	 * @return true or false
	 */
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
			// You do not have enough Abyss Points.
			PacketSendUtility.sendPacket(player, new SM_SYSTEM_MESSAGE(1300927));
			return false;
		}

		for (Integer itemId : requiredItems.keySet()) {
			long count = player.getInventory().getItemCountByItemId(itemId);
			if (requiredItems.get(itemId) < 1 || count < requiredItems.get(itemId))
				return false;
		}

		return true;
	}

	/**
	 * @return the tradeItems
	 */
	public List<TradeItem> getTradeItems() {
		return tradeItems;
	}

	public int size() {
		return tradeItems.size();
	}

	/**
	 * @return the npcId
	 */
	public int getSellerObjId() {
		return sellerObjId;
	}

	/**
	 * @return the requiredAp
	 */
	public int getRequiredAp() {
		return requiredAp;
	}

	/**
	 * @return the requiredKinah
	 */
	public long getRequiredKinah() {
		return requiredKinah;
	}

	/**
	 * @return the requiredItems
	 */
	public Map<Integer, Long> getRequiredItems() {
		return requiredItems;
	}
}
