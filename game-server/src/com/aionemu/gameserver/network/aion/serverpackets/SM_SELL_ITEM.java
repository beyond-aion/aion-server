package com.aionemu.gameserver.network.aion.serverpackets;

import java.util.List;

import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.templates.tradelist.TradeListTemplate;
import com.aionemu.gameserver.model.templates.tradelist.TradeNpcType;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;
import com.aionemu.gameserver.services.trade.PricesService;

/**
 * @author orz, Sarynth, Artur, Neon
 */
public class SM_SELL_ITEM extends AionServerPacket {

	private final int targetObjectId;
	private final TradeNpcType tradeNpcType;
	private final int buyPriceRate;
	private final boolean showBuyTab;
	private final boolean showSellTab;
	private final List<Integer> tradeTabIds;

	public SM_SELL_ITEM(Npc npc) {
		TradeListTemplate tradeList = DataManager.TRADE_LIST_DATA.getPurchaseTemplate(npc.getNpcId());
		this.targetObjectId = npc.getObjectId();
		this.tradeNpcType = tradeList != null ? tradeList.getTradeNpcType() : TradeNpcType.NORMAL;
		this.buyPriceRate = tradeList != null ? tradeList.getBuyPriceRate() : PricesService.getVendorSellModifier();
		this.showBuyTab = npc.canSell();
		this.showSellTab = npc.canBuy() || npc.canPurchase();
		this.tradeTabIds = tradeList != null ? tradeList.getTradeTabIds() : List.of();
	}

	@Override
	protected void writeImpl(AionConnection con) {
		writeD(targetObjectId);
		writeC(tradeNpcType.index());
		writeD(buyPriceRate); // price * (buyPriceRate / 100) = display price
		writeC(showBuyTab ? 1 : 0); // npc sells
		writeC(showSellTab ? 1 : 0); // npc buys
		writeH(tradeTabIds.size());
		for (Integer tradeTabId : tradeTabIds)
			writeD(tradeTabId);
	}
}
