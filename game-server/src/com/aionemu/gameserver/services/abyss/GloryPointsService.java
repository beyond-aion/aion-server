package com.aionemu.gameserver.services.abyss;

import com.aionemu.commons.database.dao.DAOManager;
import com.aionemu.gameserver.configs.main.RatesConfig;
import com.aionemu.gameserver.dao.AbyssRankDAO;
import com.aionemu.gameserver.model.gameobjects.player.AbyssRank;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.player.Rates;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ABYSS_RANK;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.world.World;

/**
 * @author ViAl
 */
public class GloryPointsService {

	public static void increaseGp(int playerObjId, int additionalGp) {
		increaseGp(playerObjId, additionalGp, true);
	}

	public static void increaseGp(int playerObjId, int additionalGp, boolean addRates) {
		Player onlinePlayer = World.getInstance().findPlayer(playerObjId);
		if (onlinePlayer != null) {
			addGp(onlinePlayer, additionalGp, addRates);
		} else {
			if (addRates) { // TODO: different memberships
				additionalGp *= RatesConfig.GP_RATES[0];
			}
			DAOManager.getDAO(AbyssRankDAO.class).increaseGp(playerObjId, additionalGp);
		}
	}

	public static void decreaseGp(int playerObjId, int gpToRemove) {
		Player onlinePlayer = World.getInstance().findPlayer(playerObjId);
		if (onlinePlayer != null) {
			addGp(onlinePlayer, -gpToRemove, false);
		} else {
			DAOManager.getDAO(AbyssRankDAO.class).decreaseGp(playerObjId, gpToRemove);
		}
	}

	public static void addGp(Player player, int additionalGp) {
		addGp(player, additionalGp, true);
	}

	public static void addGp(Player player, int additionalGp, boolean addRates) {
		if (player == null)
			return;
		AbyssRank rank = player.getAbyssRank();
		if (addRates)
			additionalGp = (int) Rates.GP.calcResult(player, additionalGp);
		rank.addGp(additionalGp);
		if (additionalGp > 0)
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_GLORY_POINT_GAIN(additionalGp));
		else
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_GLORY_POINT_LOSE(additionalGp * -1));
		PacketSendUtility.sendPacket(player, new SM_ABYSS_RANK(player.getAbyssRank()));
	}
}
