package com.aionemu.gameserver.network.aion.clientpackets;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection.State;
import com.aionemu.gameserver.services.TradeService;

/**
 * @author MrPoke, Ritsu
 */
public class CM_BUY_TRADE_IN_TRADE extends AionClientPacket {

	private int sellerObjId;
	@SuppressWarnings("unused")
	private byte mask;
	private int itemId;
	private int count;
	private int tradeInListCount;
	private List<Integer> tradeInItemObjIds;

	public CM_BUY_TRADE_IN_TRADE(int opcode, Set<State> validStates) {
		super(opcode, validStates);
	}

	@Override
	protected void readImpl() {
		tradeInItemObjIds = new ArrayList<>();
		sellerObjId = readD();
		mask = readC(); // NEW - TODO find out what this is!
		itemId = readD();
		count = readD();
		tradeInListCount = readUH();
		for (int i = 0; i < tradeInListCount; i++)
			tradeInItemObjIds.add(readD());
	}

	@Override
	protected void runImpl() {
		Player player = this.getConnection().getActivePlayer();
		if (count < 1)
			return;

		TradeService.performBuyFromTradeInTrade(player, sellerObjId, itemId, count, tradeInItemObjIds);
	}
}
