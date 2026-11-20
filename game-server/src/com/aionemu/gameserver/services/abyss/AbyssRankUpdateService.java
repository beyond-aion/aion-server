package com.aionemu.gameserver.services.abyss;

import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.configs.main.RankingConfig;
import com.aionemu.gameserver.dao.AbyssRankDAO;
import com.aionemu.gameserver.dao.AbyssRankDAO.AbyssRankEntry;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.abyss.AbyssRankTemplate;
import com.aionemu.gameserver.services.cron.CronService;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.utils.stats.AbyssRankEnum;
import com.aionemu.gameserver.world.World;

/**
 * @author ATracer, Neon
 */
public class AbyssRankUpdateService {

	private static final Logger log = LoggerFactory.getLogger(AbyssRankUpdateService.class);

	private AbyssRankUpdateService() {
	}

	public static void scheduleUpdate() {
		log.info("Scheduling the daily abyss rank update (GP loss and rank update) based on cron expression: {}", RankingConfig.TOP_RANKING_UPDATE_RULE);
		CronService.getInstance().schedule(AbyssRankUpdateService::performDailyUpdate, RankingConfig.TOP_RANKING_UPDATE_RULE, true);
	}

	/**
	 * Applies the daily GP loss and then refreshes the leaderboard and quota ranks, so the daily rank recalculation happens in one pass.
	 */
	public static void performDailyUpdate() {
		updateDailyGpLoss();
		performUpdate();
	}

	/**
	 * Perform update of all ranks
	 */
	public static void performUpdate() {
		ThreadPoolManager.getInstance().schedule(() -> {
			log.info("AbyssRankUpdateService: Executing rank update...");
			long startTime = System.currentTimeMillis();

			// update and store rank statistics for all online players (offline players update on login)
			World.getInstance().forEachPlayer(player -> {
				player.getAbyssRank().doUpdate();
				AbyssRankDAO.storeAbyssRank(player);
			});

			int playerLimit = Math.max(DataManager.ABYSS_RANK_TABLE.getMaxRequireOrder(Race.ELYOS),
				DataManager.ABYSS_RANK_TABLE.getMaxRequireOrder(Race.ASMODIANS));

			// update and store player & legion DB rank_pos entries (for ▼/▲/= trend in the ranking table)
			AbyssRankDAO.updateRankingLists(RankingConfig.TOP_RANKING_MAX_OFFLINE_DAYS, playerLimit, RankingConfig.RANKING_LIST_LEGION_LIMIT);
			// update and store GP ranks
			updateRanksForRace(Race.ASMODIANS);
			updateRanksForRace(Race.ELYOS);

			// update ranking cache
			AbyssRankingCache.getInstance().reloadRankings();

			log.info("AbyssRankUpdateService: Finished in {} seconds", (System.currentTimeMillis() - startTime) / 1000);
		}, 1000);
	}

	/**
	 * Assigns the ranks of all players of the race from their AP, GP and new ranking position (see
	 * {@link com.aionemu.gameserver.dataholders.AbyssRankTable#getRankForPointsAndPosition}). The positions are numbered by GP, so this equals filling
	 * the rank quotas from the top. Online players also receive their new position.
	 */
	private static void updateRanksForRace(Race race) {
		List<AbyssRankEntry> entries = AbyssRankDAO.loadAbyssRankEntries(race);
		if (entries == null)
			return;
		for (AbyssRankEntry entry : entries) {
			AbyssRankTemplate rank = DataManager.ABYSS_RANK_TABLE.getRankForPointsAndPosition(race, entry.ap(), entry.gp(), entry.rankPos());
			if (rank != null && (rank.getRank() != entry.rank() || World.getInstance().getPlayer(entry.playerId()) != null))
				updateRankTo(rank.getRankEnum(), entry.playerId(), entry.rankPos());
		}
	}

	private static void updateRankTo(AbyssRankEnum newRank, int playerId, int rankingPosition) {
		// check if rank has changed for online players
		Player player = World.getInstance().getPlayer(playerId);
		if (player != null) {
			boolean rankChanged = player.getAbyssRank().getRank() != newRank;
			if (rankChanged) {
				player.getAbyssRank().setRank(newRank);
				// save to db now, so cache reload loads the correct rank for online players
				AbyssRankDAO.updateAbyssRank(playerId, newRank);
			}
			AbyssPointsService.onRankChanged(player, false, rankChanged, rankingPosition);
		} else {
			AbyssRankDAO.updateAbyssRank(playerId, newRank);
		}
	}

	private static void updateDailyGpLoss() {
		for (Race race : List.of(Race.ELYOS, Race.ASMODIANS)) {
			for (AbyssRankEnum rank : AbyssRankEnum.values()) {
				AbyssRankTemplate template = DataManager.ABYSS_RANK_TABLE.get(race, rank);
				if (template.getDailyReduceGp() > 0)
					AbyssRankDAO.dailyUpdateGp(race, rank.getId(), template.getDailyReduceGp());
			}
		}
		for (Player p : World.getInstance().getAllPlayers()) {
			AbyssRankTemplate template = DataManager.ABYSS_RANK_TABLE.get(p.getRace(), p.getAbyssRank().getRank());
			if (template != null && template.getDailyReduceGp() > 0)
				GloryPointsService.addGp(p.getObjectId(), -template.getDailyReduceGp());
		}
	}
}
