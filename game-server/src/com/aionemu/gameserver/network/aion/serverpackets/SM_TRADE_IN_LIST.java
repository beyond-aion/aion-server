package com.aionemu.gameserver.network.aion.serverpackets;

import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.templates.tradelist.TradeListTemplate;
import com.aionemu.gameserver.model.templates.tradelist.TradeNpcType;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;

import java.util.List;

/**
 * @author MrPoke
 */
public class SM_TRADE_IN_LIST extends AionServerPacket {

	private final Npc npc;
	private final TradeNpcType tradeNpcType;
	private final int buyPriceModifier;
	private final List<Integer> tradeTabIds;

	public SM_TRADE_IN_LIST(Npc npc, TradeListTemplate tradeList, int buyPriceModifier) {
		this.npc = npc;
		this.tradeNpcType = tradeList.getTradeNpcType();
		this.buyPriceModifier = buyPriceModifier;
		this.tradeTabIds = tradeList.getTradeTabIds();
	}

	@Override
	protected void writeImpl(AionConnection con) {
		writeD(npc.getObjectId());
		writeC(tradeNpcType.index());
		writeD(buyPriceModifier); // Vendor Buy Price Modifier
		writeD(100);// new aion 4.5
		writeH(tradeTabIds.size());
		for (Integer tradeTabId : tradeTabIds)
			writeD(tradeTabId);
	}
}
