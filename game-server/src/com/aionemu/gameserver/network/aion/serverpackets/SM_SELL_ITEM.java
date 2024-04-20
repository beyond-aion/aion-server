package com.aionemu.gameserver.network.aion.serverpackets;

import java.util.ArrayList;
import java.util.List;

import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.templates.tradelist.TradeListTemplate;
import com.aionemu.gameserver.model.templates.tradelist.TradeListTemplate.TradeTab;
import com.aionemu.gameserver.model.templates.tradelist.TradeNpcType;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;
import com.aionemu.gameserver.services.trade.PricesService;

/**
 * @author orz, Sarynth, Artur, Neon
 */
public class SM_SELL_ITEM extends AionServerPacket {

	private int targetObjectId;
	private TradeNpcType tradeNpcType;
	private int buyPriceRate;
	private boolean showBuyTab;
	private boolean showSellTab;
	private List<TradeTab> tradeTabs;

	public SM_SELL_ITEM(Npc npc) {
		TradeListTemplate tradeList = DataManager.TRADE_LIST_DATA.getPurchaseTemplate(npc.getNpcId());
		this.targetObjectId = npc.getObjectId();
		this.tradeNpcType = tradeList != null ? tradeList.getTradeNpcType() : TradeNpcType.NORMAL;
		this.buyPriceRate = tradeList != null ? tradeList.getBuyPriceRate() : PricesService.getVendorSellModifier();
		this.showBuyTab = npc.canSell();
		this.showSellTab = npc.canBuy() || npc.canPurchase();
		this.tradeTabs = tradeList != null ? tradeList.getTradeTablist() : new ArrayList<>();
	}

	@Override
	protected void writeImpl(AionConnection con) {
		writeD(targetObjectId);
		writeC(tradeNpcType.index());
		writeD(buyPriceRate); // price * (buyPriceRate / 100) = display price
		writeC(showBuyTab ? 1 : 0); // npc sells
		writeC(showSellTab ? 1 : 0); // npc buys
		writeH(tradeTabs.size());
		for (TradeTab tradeTab : tradeTabs)
			writeD(tradeTab.getId());
	}
}
