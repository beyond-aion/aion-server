package com.aionemu.gameserver.services.abyss;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javolution.util.FastTable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.database.dao.DAOManager;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.commons.services.CronService;
import com.aionemu.gameserver.configs.main.RankingConfig;
import com.aionemu.gameserver.dao.AbyssRankDAO;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.gameobjects.player.AbyssRank;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.utils.stats.AbyssRankEnum;
import com.aionemu.gameserver.world.World;
import com.aionemu.gameserver.world.knownlist.Visitor;

/**
 * @author ATracer
 * @modified Neon
 */
public class AbyssRankUpdateService {

	private static final Logger log = LoggerFactory.getLogger(AbyssRankUpdateService.class);

	private AbyssRankUpdateService() {
	}

	public static AbyssRankUpdateService getInstance() {
		return SingletonHolder.instance;
	}

	public void scheduleUpdate() {
		log.info("Scheduling ranking update task based on cron expression: " + RankingConfig.TOP_RANKING_UPDATE_RULE);
		CronService.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				performUpdate();
			}
		}, RankingConfig.TOP_RANKING_UPDATE_RULE, true);

		log.info("Scheduling daily GP loss task based on cron expression: " + RankingConfig.TOP_RANKING_DAILY_GP_LOSS_TIME);
		CronService.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				updateDailyGpLoss();
			}
		}, RankingConfig.TOP_RANKING_DAILY_GP_LOSS_TIME, true);
	}

	/**
	 * Perform update of all ranks
	 */
	public void performUpdate() {
		ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				log.info("AbyssRankUpdateService: Executing rank update...");
				long startTime = System.currentTimeMillis();

				// update and store rank statistics for all online players (offline players update on login)
				World.getInstance().forEachPlayer(new Visitor<Player>() {

					@Override
					public void visit(Player player) {
						player.getAbyssRank().doUpdate();
						DAOManager.getDAO(AbyssRankDAO.class).storeAbyssRank(player);
					}
				});

				// update and store GP ranks
				updateQuotaRanksForRace(Race.ASMODIANS, AbyssRankEnum.STAR1_OFFICER, RankingConfig.TOP_RANKING_MAX_OFFLINE_DAYS);
				updateQuotaRanksForRace(Race.ELYOS, AbyssRankEnum.STAR1_OFFICER, RankingConfig.TOP_RANKING_MAX_OFFLINE_DAYS);

				// update and store player & legion DB rank_pos entries (for ▼/▲/= trend in the ranking table)
				DAOManager.getDAO(AbyssRankDAO.class).updateRankList(RankingConfig.TOP_RANKING_MAX_OFFLINE_DAYS);

				// update ranking cache
				AbyssRankingCache.getInstance().reloadRankings();

				log.info("AbyssRankUpdateService: Finished in " + (System.currentTimeMillis() - startTime) / 1000 + "s");
			}
		}, 1000);
	}

	/**
	 * Update player ranks based on quota for all players (online/offline)
	 * 
	 * @param race
	 *          the race that will be updated
	 * @param limitRank
	 *          the minimum rank that will be updated (all lower GP ranks are disabled)
	 * @param activeAfterDays
	 *          if greater than 0, players who were offline for that period will lose their rank
	 */
	private void updateQuotaRanksForRace(Race race, AbyssRankEnum limitRank, int maxOfflineDays) {
		Map<Integer, Integer[]> playerGpApMap = DAOManager.getDAO(AbyssRankDAO.class).loadPlayersGpAp(race, limitRank, maxOfflineDays);
		List<Entry<Integer, Integer[]>> playerGpApEntries = new FastTable<>();
		playerGpApEntries.addAll(playerGpApMap.entrySet());
		playerGpApEntries.sort(new PlayerGpComparator());

		// calculate and set new GP ranks
		for (int i = AbyssRankEnum.SUPREME_COMMANDER.getId(); i >= limitRank.getId(); i--)
			selectAndUpdateQuotaRank(AbyssRankEnum.getRankById(i), playerGpApEntries);

		// set all players left in the list to AP ranks
		updateToNoQuotaRank(playerGpApEntries);
	}

	private void selectAndUpdateQuotaRank(AbyssRankEnum rank, List<Entry<Integer, Integer[]>> playerGpApEntries) {
		int quota = rank.getId() < AbyssRankEnum.SUPREME_COMMANDER.getId() ? (rank.getQuota() - AbyssRankEnum.getRankById(rank.getId() + 1).getQuota())
			: rank.getQuota();

		for (int i = 0; i < quota; i++) {
			if (playerGpApEntries.isEmpty()) {
				return;
			}
			// check next player in list
			Entry<Integer, Integer[]> playerGpApEntry = playerGpApEntries.get(0);
			// check if there are some players left in map
			if (playerGpApEntry == null) {
				return;
			}
			int playerId = playerGpApEntry.getKey();
			int gp = playerGpApEntry.getValue()[0];
			// check if this (and the rest) player has required GP count
			if (gp < rank.getRequiredGP()) {
				return;
			}
			// remove player and update its rank
			playerGpApEntries.remove(0);
			updateRankTo(rank, playerId);
		}
	}

	private void updateToNoQuotaRank(List<Entry<Integer, Integer[]>> playerGpApEntries) {
		for (Entry<Integer, Integer[]> playerGpEntry : playerGpApEntries) {
			AbyssRankEnum rank = AbyssRankEnum.getRankForPoints(playerGpEntry.getValue()[1], 0); // no GP -> no officer ranks
			updateRankTo(rank, playerGpEntry.getKey());
		}
	}

	protected void updateRankTo(AbyssRankEnum newRank, int playerId) {
		// check if rank has changed for online players
		Player onlinePlayer = World.getInstance().findPlayer(playerId);
		if (onlinePlayer != null) {
			AbyssRank abyssRank = onlinePlayer.getAbyssRank();
			AbyssRankEnum currentRank = abyssRank.getRank();
			if (currentRank != newRank) {
				abyssRank.setRank(newRank);
				// update skills and equip
				AbyssPointsService.checkRankChanged(onlinePlayer, currentRank, newRank);
			}
		} else {
			DAOManager.getDAO(AbyssRankDAO.class).updateAbyssRank(playerId, newRank);
		}
	}

	private void updateDailyGpLoss() {
		DAOManager.getDAO(AbyssRankDAO.class).dailyUpdateGp(AbyssRankEnum.STAR1_OFFICER);
		DAOManager.getDAO(AbyssRankDAO.class).dailyUpdateGp(AbyssRankEnum.STAR2_OFFICER);
		DAOManager.getDAO(AbyssRankDAO.class).dailyUpdateGp(AbyssRankEnum.STAR3_OFFICER);
		DAOManager.getDAO(AbyssRankDAO.class).dailyUpdateGp(AbyssRankEnum.STAR4_OFFICER);
		DAOManager.getDAO(AbyssRankDAO.class).dailyUpdateGp(AbyssRankEnum.STAR5_OFFICER);
		DAOManager.getDAO(AbyssRankDAO.class).dailyUpdateGp(AbyssRankEnum.GENERAL);
		DAOManager.getDAO(AbyssRankDAO.class).dailyUpdateGp(AbyssRankEnum.GREAT_GENERAL);
		DAOManager.getDAO(AbyssRankDAO.class).dailyUpdateGp(AbyssRankEnum.COMMANDER);
		DAOManager.getDAO(AbyssRankDAO.class).dailyUpdateGp(AbyssRankEnum.SUPREME_COMMANDER);
		for (Player p : World.getInstance().getAllPlayers()) {
			if (p.getAbyssRank().getRank().getId() >= AbyssRankEnum.STAR1_OFFICER.getId()) {
				GloryPointsService.addGp(p, -p.getAbyssRank().getRank().getGpLossPerDay());
			}
		}
	}

	private static class SingletonHolder {

		protected static final AbyssRankUpdateService instance = new AbyssRankUpdateService();
	}

	private static class PlayerGpComparator implements Comparator<Entry<Integer, Integer[]>> {

		@Override
		public int compare(Entry<Integer, Integer[]> o1, Entry<Integer, Integer[]> o2) {
			return -(o1.getValue()[0]).compareTo(o2.getValue()[0]);
		}
	}
}
