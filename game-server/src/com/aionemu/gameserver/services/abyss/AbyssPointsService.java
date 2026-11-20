package com.aionemu.gameserver.services.abyss;

import java.sql.Timestamp;
import java.util.function.IntFunction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.dao.AbyssRankDAO;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.gameobjects.player.AbyssRank;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.player.Rates;
import com.aionemu.gameserver.model.templates.abyss.AbyssRankTemplate;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ABYSS_RANK;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ABYSS_RANK_UPDATE;
import com.aionemu.gameserver.network.aion.serverpackets.SM_LEGION_EDIT;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.AbyssPointLimitService;
import com.aionemu.gameserver.services.SiegeService;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.stats.AbyssRankEnum;

/**
 * @author ATracer
 */
public class AbyssPointsService {

	private static final Logger log = LoggerFactory.getLogger(AbyssPointsService.class);
	private static final int DORMANT_GRACE_DAYS = 30;

	/**
	 * Adds the AP for a killed player or NPC like retail: the rank difference modifier, then the AP limits, then the rates, which include the AP
	 * boost.
	 *
	 * @param victimRankId
	 *          Abyss rank id of the victim, 0 if it has none.
	 * @return The AP after the rank difference modifier, also if a limit stopped the gain; the AP loss of a killed player is based on it.
	 */
	public static int addKillAp(Player player, VisibleObject victim, int victimRankId, int ap, Rates rates) {
		if (ap <= 0)
			return 0;
		AbyssRankTemplate rank = DataManager.ABYSS_RANK_TABLE.get(player.getRace(), player.getAbyssRank().getRank());
		if (rank != null)
			ap = rank.applyRankDiffMod(ap, victimRankId);
		if (ap > 0 && AbyssPointLimitService.getInstance().canGainAp(player, victim.getObjectId(), victimRankId, ap))
			addAp(player, victim, rates.calcResult(player, ap));
		return ap;
	}

	public static void addAp(Player player, VisibleObject obj, int value) {
		if (value > 30000) {
			log.warn("WARN BIG COUNT AP: {} for {} from {}", value, player, obj);
		}
		addAp(player, value);
		SiegeService.getInstance().onAbyssPointsAdded(player, obj, value);
	}

	public static void addAp(Player player, int amount) {
		addAp(player, amount, SM_SYSTEM_MESSAGE::STR_MSG_COMBAT_MY_ABYSS_POINT_GAIN);
	}

	public static void addAp(Player player, int amount, IntFunction<SM_SYSTEM_MESSAGE> gainMessage) {
		if (player == null)
			return;

		int oldAp = player.getAbyssRank().getAp();
		AbyssRankEnum oldAbyssRank = player.getAbyssRank().getRank();
		player.getAbyssRank().addAp(amount);
		int added = player.getAbyssRank().getAp() - oldAp;
		revalidateRank(player, false);

		SM_SYSTEM_MESSAGE msg = amount >= 0 ? gainMessage.apply(added) : SM_SYSTEM_MESSAGE.STR_MSG_USE_ABYSSPOINT(-added);
		PacketSendUtility.sendPacket(player, msg);
		onRankChanged(player, added != 0, oldAbyssRank != player.getAbyssRank().getRank(), null);
		if (player.isLegionMember() && added > 0) {
			player.getLegion().addContributionPoints(added);
			PacketSendUtility.broadcastToLegion(player.getLegion(), new SM_LEGION_EDIT(0x03, player.getLegion()));
		}
	}

	/**
	 * Recalculates the abyss rank after an AP or GP change and on login. Officer ranks are only assigned by the daily update; here they can only be
	 * lost, when the player falls below the first officer rank's requirements. All other ranks follow the AP.
	 *
	 * @return true if the rank changed
	 */
	public static boolean revalidateRank(Player player, boolean notify) {
		if (player == null)
			return false;
		AbyssRank abyssRank = player.getAbyssRank();
		AbyssRankTemplate newRank = DataManager.ABYSS_RANK_TABLE.revalidate(player.getRace(), abyssRank.getRank(), abyssRank.getAp(),
			abyssRank.getCurrentGP());
		if (newRank == null || newRank.getRankEnum() == abyssRank.getRank())
			return false;
		abyssRank.setRank(newRank.getRankEnum());
		AbyssRankDAO.updateAbyssRank(player.getObjectId(), newRank.getRankEnum());
		if (notify)
			onRankChanged(player, false, true, null);
		return true;
	}

	/**
	 * Applies the GP loss for a player who missed more than the grace period of leaderboard updates. The days are counted from the last ranking
	 * update, not from the current time: the loss covers the period in which the leaderboard stood still. The daily amount comes from the rank
	 * selected by GP alone, because officer ranks are defined by their GP requirement and a player below the matching position still pays the
	 * officer's loss; the result is capped at the current GP and goes through the normal GP path, which notifies and revalidates the rank.
	 */
	public static void applyDormantGloryPointLoss(Player player, Timestamp lastOnline) {
		if (player == null || lastOnline == null)
			return;
		int gp = player.getAbyssRank().getCurrentGP();
		long days = getDormantDays(lastOnline.getTime() / 1000, AbyssRankingCache.getInstance().getLastUpdate());
		if (days <= DORMANT_GRACE_DAYS)
			return;
		AbyssRankTemplate rank = DataManager.ABYSS_RANK_TABLE.getRankForGp(player.getRace(), gp);
		if (rank == null)
			return;
		long loss = getDormantGpLoss(gp, days, rank.getDailyReduceGp());
		if (loss > 0)
			GloryPointsService.addGp(player.getObjectId(), (int) -loss);
	}

	/**
	 * @return Whole days between the last logout and the last ranking update; 0 when the player logged in after that update.
	 */
	static long getDormantDays(long lastOnlineSeconds, long rankingUpdateSeconds) {
		if (lastOnlineSeconds <= 0 || rankingUpdateSeconds <= lastOnlineSeconds)
			return 0;
		return (rankingUpdateSeconds - lastOnlineSeconds) / (24L * 60 * 60);
	}

	/**
	 * @return GP to subtract for a dormant player: the daily loss of the rank for every full day past the grace period, capped at the current
	 *         GP. The emulator has no data-cutover date, so the additional cap by the time since that cutover is not modelled.
	 */
	static long getDormantGpLoss(int gp, long daysAfterLogout, int dailyReduceGp) {
		if (gp <= 0 || dailyReduceGp <= 0 || daysAfterLogout <= DORMANT_GRACE_DAYS)
			return 0;
		return Math.min((long) dailyReduceGp * (daysAfterLogout - DORMANT_GRACE_DAYS), gp);
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
