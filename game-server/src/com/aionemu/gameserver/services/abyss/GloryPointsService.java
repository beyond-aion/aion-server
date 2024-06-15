package com.aionemu.gameserver.services.abyss;

import com.aionemu.gameserver.configs.main.RatesConfig;
import com.aionemu.gameserver.dao.AbyssRankDAO;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.player.Rates;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ABYSS_RANK;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.world.World;

/**
 * @author ViAl, Sykra
 */
public class GloryPointsService {

	private GloryPointsService() {
	}

	public static void modifyGpBy(int playerObjId, int amount) {
		modifyGpBy(playerObjId, amount, true, true);
	}

	public static void modifyGpBy(int playerObjId, int amount, boolean applyRatesOnIncrease, boolean modifyStatsOnIncrease) {
		if (amount == 0)
			return;
		if (amount > 0)
			increaseGpBy(playerObjId, amount, applyRatesOnIncrease, modifyStatsOnIncrease);
		else
			decreaseGpBy(playerObjId, amount);
	}

	public static void increaseGpBy(int playerObjId, int amount) {
		increaseGpBy(playerObjId, amount, true, true);
	}

	@SuppressWarnings("lossy-conversions")
	public static void increaseGpBy(int playerObjId, int amount, boolean applyRates, boolean modifyStats) {
		if (amount == 0)
			return;
		Player player = World.getInstance().getPlayer(playerObjId);
		if (player == null) {
			if (applyRates)
				amount *= RatesConfig.GP_RATES[0]; // TODO different memberships
			AbyssRankDAO.increaseGp(playerObjId, amount, modifyStats);
		} else {
			if (applyRates)
				amount = (int) Rates.GP.calcResult(player, amount);
			player.getAbyssRank().increaseGp(amount, modifyStats);
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_GLORY_POINT_GAIN(amount));
			PacketSendUtility.sendPacket(player, new SM_ABYSS_RANK(player));
		}
	}

	public static void decreaseGpBy(int playerObjId, int amount) {
		if (amount == 0)
			return;
		Player player = World.getInstance().getPlayer(playerObjId);
		if (player == null) {
			AbyssRankDAO.decreaseGp(playerObjId, amount);
		} else {
			player.getAbyssRank().reduceGp(amount);
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_GLORY_POINT_LOSE(amount));
			PacketSendUtility.sendPacket(player, new SM_ABYSS_RANK(player));
		}
	}

}
