package com.aionemu.gameserver.network.aion.serverpackets;

import java.util.List;

import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.limiteditems.LimitedItem;
import com.aionemu.gameserver.model.limiteditems.LimitedTradeNpc;
import com.aionemu.gameserver.model.templates.tradelist.TradeNpcType;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;
import com.aionemu.gameserver.services.LimitedItemTradeService;

/**
 * @author alexa026, ATracer, Sarynth, xTz, Neon
 */
public class SM_TRADELIST extends AionServerPacket {

	private final int targetObjId;
	private final int playerObjId;
	private final TradeNpcType tradeNpcType;
	private final int buyPriceModifier;
	private final boolean showBuyTab;
	private final boolean showSellTab;
	private final List<Integer> tradeTabIds;
	private final List<LimitedItem> limitedItems;

	public SM_TRADELIST(Player player, Npc npc, TradeNpcType tradeNpcType, List<Integer> tradeTabIds, int buyPriceModifier) {
		this.targetObjId = npc.getObjectId();
		this.playerObjId = player.getObjectId();
		this.tradeNpcType = tradeNpcType;
		this.buyPriceModifier = buyPriceModifier;
		this.showBuyTab = npc.canSell();
		this.showSellTab = npc.canBuy();
		this.tradeTabIds = tradeTabIds;
		LimitedTradeNpc limitedTradeNpc = LimitedItemTradeService.getInstance().getLimitedTradeNpc(npc.getNpcId());
		this.limitedItems = limitedTradeNpc == null ? List.of() : limitedTradeNpc.getLimitedItems();
	}

	@Override
	protected void writeImpl(AionConnection con) {
		writeD(targetObjId);
		writeC(tradeNpcType.index()); // reward, abyss or normal
		writeD(buyPriceModifier); // Vendor Buy Price Modifier
		writeD(100);// new aion 4.5
		writeC(showBuyTab ? 1 : 0);
		writeC(showSellTab ? 1 : 0);
		writeH(tradeTabIds.size());
		for (Integer tradeTabId : tradeTabIds)
			writeD(tradeTabId);
		writeH(limitedItems.size());
		for (LimitedItem limitedItem : limitedItems) {
			writeD(limitedItem.getItemId());
			writeH(limitedItem.getBuyCount(playerObjId));
			writeH(limitedItem.getSellLimit());
		}
	}
}
