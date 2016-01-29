package com.aionemu.gameserver.network.aion.clientpackets;

import java.util.Set;

import javolution.util.FastSet;

import com.aionemu.gameserver.configs.main.AntiHackConfig;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection.State;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.TradeService;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author MrPoke
 * @rework Ritsu
 */
public class CM_BUY_TRADE_IN_TRADE extends AionClientPacket {

	private int sellerObjId;
	@SuppressWarnings("unused")
	private int mask;
	private int itemId;
	private int count;
	private int tradeInListCount;
	private Set<Integer> tradeInItemObjIds;

	/**
	 * @param opcode
	 */
	public CM_BUY_TRADE_IN_TRADE(int opcode, State state, State... restStates) {
		super(opcode, state, restStates);
	}

	@Override
	protected void readImpl() {
		tradeInItemObjIds = new FastSet<>();
		sellerObjId = readD();
		mask = readC(); // NEW - TODO find out what this is!
		itemId = readD();
		count = readD();
		tradeInListCount = readH();
		for (int i = 0; i < tradeInListCount; i++)
			tradeInItemObjIds.add(readD());
	}

	@Override
	protected void runImpl() {
		Player player = this.getConnection().getActivePlayer();
		if (count < 1)
			return;

		if (player.getPlayerAccount().isHacked() && !AntiHackConfig.HDD_SERIAL_HACKED_ACCOUNTS_ALLOW_TRADE) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_L2AUTH_S_KICKED_DOUBLE_LOGIN);
			PacketSendUtility.sendMessage(player, "Account hacking attempt detected. You can't use this function. Please, contact your server support.");
			return;
		}

		TradeService.performBuyFromTradeInTrade(player, sellerObjId, itemId, count, tradeInItemObjIds);
	}
}
