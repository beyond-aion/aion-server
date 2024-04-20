package com.aionemu.gameserver.network.aion.serverpackets;

import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;
import com.aionemu.gameserver.services.trade.PricesService;

/**
 * @author xavier, Sarynth, Wakizashi (Price/tax in Influence ration dialog)
 */
public class SM_PRICES extends AionServerPacket {

	@Override
	protected void writeImpl(AionConnection con) {
		writeC(PricesService.getGlobalPrices(con.getActivePlayer().getRace())); // Display Buying Price
																																						// %
		writeC(PricesService.getGlobalPricesModifier()); // Buying Modified Price %
		writeC(PricesService.getTaxes(con.getActivePlayer().getRace())); // Tax = -100 + C %
	}
}
