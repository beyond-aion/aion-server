package com.aionemu.gameserver.network.aion.serverpackets;

import java.util.List;

import javolution.util.FastTable;

import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.limiteditems.LimitedItem;
import com.aionemu.gameserver.model.limiteditems.LimitedTradeNpc;
import com.aionemu.gameserver.model.templates.goods.GoodsList;
import com.aionemu.gameserver.model.templates.tradelist.TradeListTemplate;
import com.aionemu.gameserver.model.templates.tradelist.TradeListTemplate.TradeTab;
import com.aionemu.gameserver.model.templates.tradelist.TradeNpcType;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;
import com.aionemu.gameserver.services.LimitedItemTradeService;

/**
 * @author alexa026, ATracer, Sarynth, xTz
 */
public class SM_TRADELIST extends AionServerPacket {

	private Integer playerObj;
	private int npcObj;
	private int npcId;
	private TradeNpcType tradeNpcType;
	private List<TradeTab> tradeTablist;
	private int buyPriceModifier;

	public SM_TRADELIST(Player player, Npc npc, TradeListTemplate tlist, int buyPriceModifier) {
		playerObj = player.getObjectId();
		int legionLevel = player.getLegion() == null ? 0 : player.getLegion().getLegionLevel();
		if (tlist != null && tlist.getNpcId() == npc.getNpcId()) {
			this.npcId = npc.getNpcId();
			this.tradeTablist = new FastTable<>();
			for (TradeTab tab : tlist.getTradeTablist()) {
				GoodsList goodsList = DataManager.GOODSLIST_DATA.getGoodsListById(tab.getId());
				if (goodsList == null || goodsList.getLegionLevel() > legionLevel)
					continue;
				this.tradeTablist.add(tab);
			}
			if (tradeTablist.size() == 0)
				this.tradeTablist = null;
			this.npcObj = npc.getObjectId();
			this.tradeNpcType = tlist.getTradeNpcType();
			this.buyPriceModifier = buyPriceModifier;
		}
	}

	@Override
	protected void writeImpl(AionConnection con) {
		if (tradeTablist != null) {
			writeD(npcObj);
			writeC(tradeNpcType.index()); // reward, abyss or normal
			writeD(buyPriceModifier); // Vendor Buy Price Modifier
			writeD(100);// new aion 4.5
			writeC(1);// new 4.3 looks like some flags. Seen value 257 for npc 279052, in binary 100000001
			writeC(1);
			writeH(tradeTablist.size());
			for (TradeTab tradeTabl : tradeTablist) {
				writeD(tradeTabl.getId());
			}

			int i = 0;
			LimitedTradeNpc limitedTradeNpc = null;
			if (LimitedItemTradeService.getInstance().isLimitedTradeNpc(npcId)) {
				limitedTradeNpc = LimitedItemTradeService.getInstance().getLimitedTradeNpc(npcId);
				i = limitedTradeNpc.getLimitedItems().size();
			}
			writeH(i);
			if (limitedTradeNpc != null) {
				for (LimitedItem limitedItem : limitedTradeNpc.getLimitedItems()) {
					writeD(limitedItem.getItemId());
					writeH(limitedItem.getBuyCount().get(playerObj) == null ? 0 : limitedItem.getBuyCount().get(playerObj));
					writeH(limitedItem.getSellLimit());
				}
			}
		}
	}
}
