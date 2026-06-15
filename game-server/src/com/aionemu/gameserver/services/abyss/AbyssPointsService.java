package com.aionemu.gameserver.services.abyss;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ABYSS_RANK;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ABYSS_RANK_UPDATE;
import com.aionemu.gameserver.network.aion.serverpackets.SM_LEGION_EDIT;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.SiegeService;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.stats.AbyssRankEnum;

/**
 * @author ATracer
 */
public class AbyssPointsService {

	private static final Logger log = LoggerFactory.getLogger(AbyssPointsService.class);

	public static void addAp(Player player, VisibleObject obj, int value) {
		if (value > 30000) {
			log.warn("WARN BIG COUNT AP: " + value + " for " + player + " from " + obj);
		}
		addAp(player, value);
		SiegeService.getInstance().onAbyssPointsAdded(player, obj, value);
	}

	public static void addAp(Player player, int amount) {
		if (player == null)
			return;

		int oldAp = player.getAbyssRank().getAp();
		AbyssRankEnum oldAbyssRank = player.getAbyssRank().getRank();
		player.getAbyssRank().addAp(amount);
		int added = player.getAbyssRank().getAp() - oldAp;

		SM_SYSTEM_MESSAGE msg = amount >= 0 ? SM_SYSTEM_MESSAGE.STR_MSG_COMBAT_MY_ABYSS_POINT_GAIN(added) : SM_SYSTEM_MESSAGE.STR_MSG_USE_ABYSSPOINT(-added);
		PacketSendUtility.sendPacket(player, msg);
		onRankChanged(player, added != 0, oldAbyssRank != player.getAbyssRank().getRank(), null);
		if (player.isLegionMember() && added > 0) {
			player.getLegion().addContributionPoints(added);
			PacketSendUtility.broadcastToLegion(player.getLegion(), new SM_LEGION_EDIT(0x03, player.getLegion()));
		}
	}

	public static void onRankChanged(Player player, boolean abyssPointChanged, boolean abyssRankChanged, Integer newRankingListPosition) {
		if (abyssPointChanged || abyssRankChanged || newRankingListPosition != null)
			PacketSendUtility.sendPacket(player, new SM_ABYSS_RANK(player, newRankingListPosition));
		if (abyssRankChanged) {
			PacketSendUtility.broadcastPacket(player, new SM_ABYSS_RANK_UPDATE(0, player));
			player.getEquipment().checkRankLimitItems();
			AbyssSkillService.updateSkills(player);
		}
	}

}
