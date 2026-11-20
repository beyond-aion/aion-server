package com.aionemu.gameserver.services.abyss;

import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.abyss.AbyssRankTemplate;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author ATracer
 */
public class AbyssService {

	/**
	 * Inggison, Gelkmaros and Silentera Canyon share one announcement field, so a death there reaches both races and all
	 * channels of these maps. Anywhere else the announcement stays in the map instance where the player died.
	 */
	private static final int[] fieldAnnounceMaps = { 210050000, 220070000, 600010000 };

	/**
	 * The abyss itself, the Panesterra maps, the Balaurea abyss fields and the two Balaurea PvP maps.
	 */
	private static boolean isAnnounceMap(int worldId) {
		return (worldId >= 400000000 && worldId <= 459999999) || worldId == 210050000 || worldId == 220070000 || worldId == 600010000
			|| worldId == 600090000 || worldId == 600100000;
	}

	private static boolean isFieldAnnounceMap(int worldId) {
		for (int fieldMap : fieldAnnounceMaps) {
			if (fieldMap == worldId)
				return true;
		}
		return false;
	}

	public static final void announceHighRankedDeath(Player victim) {
		AbyssRankTemplate rank = DataManager.ABYSS_RANK_TABLE.get(victim.getRace(), victim.getAbyssRank().getRank());
		if (rank == null || !rank.isNotifyDie() || !isAnnounceMap(victim.getWorldId()))
			return;
		SM_SYSTEM_MESSAGE message = SM_SYSTEM_MESSAGE.STR_ABYSS_ORDER_RANKER_DIE(victim);
		if (isFieldAnnounceMap(victim.getWorldId())) {
			PacketSendUtility.broadcastToWorld(message, p -> isFieldAnnounceMap(p.getWorldId()));
		} else {
			PacketSendUtility.broadcastToMap(victim.getPosition().getWorldMapInstance(), message);
		}
	}

	public static final void announceAbyssSkillUsage(Player player, String skillL10n) {
		PacketSendUtility.broadcastToWorld(SM_SYSTEM_MESSAGE.STR_SKILL_ABYSS_SKILL_IS_FIRED(player, skillL10n),
			p -> p != player && player.getWorldType() == p.getWorldType() && !p.isInInstance());
	}
}
