package com.aionemu.gameserver.network.aion.serverpackets;

import com.aionemu.gameserver.model.templates.tradelist.TradeListTemplate;
import com.aionemu.gameserver.model.templates.tradelist.TradeListTemplate.TradeTab;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;

/**
 * @author orz, Sarynth, modified by Artur
 */
public class SM_SELL_ITEM extends AionServerPacket {

	private int targetObjectId;
	private TradeListTemplate plist;
	private int sellPercentage;
	
	public SM_SELL_ITEM(int targetObjectId, TradeListTemplate plist, int sellPercentage) {
		
		this.targetObjectId = targetObjectId;
		this.plist = plist;
		this.sellPercentage = sellPercentage;
	
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void writeImpl(AionConnection con) {
		if ((plist != null) && (plist.getNpcId() != 0) && (plist.getCount() != 0)) {
			writeD(targetObjectId);
			writeC(plist.getTradeNpcType().index());
			writeD(sellPercentage);//Buy Price * (sellPercentage / 100) = Display price.
			writeH(257); // tab type
			writeH(plist.getCount());
			for (TradeTab tradeTabl : plist.getTradeTablist()) {
				writeD(tradeTabl.getId());
			}
		}
		else  {
			writeD(targetObjectId);
			writeC(1);
			writeD(sellPercentage); // Buy Price * (sellPercentage / 100) = Display price.
			writeH(257); // tab type
			writeH(0);
		}
	}
}