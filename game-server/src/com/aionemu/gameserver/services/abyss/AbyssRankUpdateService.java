package com.aionemu.gameserver.services.abyss;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.database.dao.DAOManager;
import com.aionemu.commons.network.util.ThreadPoolManager;
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
 */
public class AbyssRankUpdateService {

	private static final Logger log = LoggerFactory.getLogger(AbyssRankUpdateService.class);

	private AbyssRankUpdateService() {
	}

	public static AbyssRankUpdateService getInstance() {
		return SingletonHolder.instance;
	}

	public void scheduleUpdate() {
		log.info("Starting ranking update task based on cron expression: " + RankingConfig.TOP_RANKING_UPDATE_RULE);
		CronService.getInstance().schedule(new Runnable() {
			@Override
			public void run() {
				performUpdate();
			}
		}, RankingConfig.TOP_RANKING_UPDATE_RULE, true);
		
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
				log.info("AbyssRankUpdateService: executing rank update");
				long startTime = System.currentTimeMillis();		
				
				World.getInstance().doOnAllPlayers(new Visitor<Player>() {

					@Override
					public void visit(Player player) {
						player.getAbyssRank().doUpdate();
						DAOManager.getDAO(AbyssRankDAO.class).storeAbyssRank(player);
					}
				});
				AbyssRankingCache.getInstance().renewLegionRanking();
				updateLimitedRanks();
				AbyssRankingCache.getInstance().reloadRankings();
				log.info("AbyssRankUpdateService: execution time: " + (System.currentTimeMillis() - startTime) / 1000);
			}
		}, 1000);
	}

	/**
	 * Update player ranks based on quota for all players (online/offline)
	 */
	private void updateLimitedRanks() {
		updateAllRanksForRace(Race.ASMODIANS, AbyssRankEnum.STAR1_OFFICER, RankingConfig.TOP_RANKING_MAX_OFFLINE_DAYS);
		updateAllRanksForRace(Race.ELYOS, AbyssRankEnum.STAR1_OFFICER, RankingConfig.TOP_RANKING_MAX_OFFLINE_DAYS);
	}

	private void updateAllRanksForRace(Race race, AbyssRankEnum limitRank, int activeAfterDays) {
		Map<Integer, Integer[]> playerGpApMap = DAOManager.getDAO(AbyssRankDAO.class).loadPlayersGpAp(race, limitRank, activeAfterDays);
		List<Entry<Integer, Integer[]>> playerGpEntries = new ArrayList<>(playerGpApMap.entrySet());
		Collections.sort(playerGpEntries, new PlayerGpComparator());

		selectRank(AbyssRankEnum.SUPREME_COMMANDER, playerGpEntries);
		selectRank(AbyssRankEnum.COMMANDER, playerGpEntries);
		selectRank(AbyssRankEnum.GREAT_GENERAL, playerGpEntries);
		selectRank(AbyssRankEnum.GENERAL, playerGpEntries);
		selectRank(AbyssRankEnum.STAR5_OFFICER, playerGpEntries);
		selectRank(AbyssRankEnum.STAR4_OFFICER, playerGpEntries);
		selectRank(AbyssRankEnum.STAR3_OFFICER, playerGpEntries);
		selectRank(AbyssRankEnum.STAR2_OFFICER, playerGpEntries);
		selectRank(AbyssRankEnum.STAR1_OFFICER, playerGpEntries);
		updateToNoQuotaRank(playerGpEntries);
	}

	private void selectRank(AbyssRankEnum rank, List<Entry<Integer, Integer[]>> playerGpApEntries) {
		int quota = rank.getId() < 18 ? (rank.getQuota() - AbyssRankEnum.getRankById(rank.getId() + 1).getQuota()) : rank.getQuota();
		for (int i = 0; i < quota; i++) {
			if (playerGpApEntries.isEmpty()) {
				return;
			}
			// check next player in list
			Entry<Integer, Integer[]> playerGp = playerGpApEntries.get(0);
			// check if there are some players left in map
			if (playerGp == null) {
				return;
			}
			int playerId = playerGp.getKey();
			int gp = playerGp.getValue()[0];
			// check if this (and the rest) player has required ap count
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
		// check if rank is changed for online players
		Player onlinePlayer = World.getInstance().findPlayer(playerId);
		if (onlinePlayer != null) {
			AbyssRank abyssRank = onlinePlayer.getAbyssRank();
			AbyssRankEnum currentRank = abyssRank.getRank();
			if (currentRank != newRank) {
				abyssRank.setRank(newRank);
				AbyssPointsService.checkRankChanged(onlinePlayer, currentRank, newRank);
			}
		}
		else {
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
		for(Player p : World.getInstance().getAllPlayers()) {
			if(p.getAbyssRank().getRank().getId() >= AbyssRankEnum.STAR1_OFFICER.getId()) {
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
