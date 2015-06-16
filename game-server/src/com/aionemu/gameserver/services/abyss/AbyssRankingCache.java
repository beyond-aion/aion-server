package com.aionemu.gameserver.services.abyss;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javolution.util.FastMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.database.dao.DAOManager;
import com.aionemu.gameserver.configs.main.RankingConfig;
import com.aionemu.gameserver.dao.AbyssRankDAO;
import com.aionemu.gameserver.model.AbyssRankingResult;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ABYSS_RANKING_LEGIONS;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ABYSS_RANKING_PLAYERS;
import com.aionemu.gameserver.services.LegionService;
import com.aionemu.gameserver.world.World;
import com.aionemu.gameserver.world.knownlist.Visitor;

/**
 * @author VladimirZ
 */
public class AbyssRankingCache {

	private static final Logger log = LoggerFactory.getLogger(AbyssRankingCache.class);
	private int lastUpdate;
	private final FastMap<Race, List<SM_ABYSS_RANKING_PLAYERS>> players = new FastMap<Race, List<SM_ABYSS_RANKING_PLAYERS>>();
	private final FastMap<Race, SM_ABYSS_RANKING_LEGIONS> legions = new FastMap<Race, SM_ABYSS_RANKING_LEGIONS>();

	public void reloadRankings() {
		log.info("Updating abyss ranking cache");
		this.lastUpdate = (int) (System.currentTimeMillis() / 1000);
		getDAO().updateRankList(RankingConfig.TOP_RANKING_MAX_OFFLINE_DAYS);

		renewPlayerRanking(Race.ASMODIANS);
		renewPlayerRanking(Race.ELYOS);

		renewLegionRanking();

		World.getInstance().doOnAllPlayers(new Visitor<Player>() {
			@Override
			public void visit(Player player) {
				player.resetAbyssRankListUpdated();
			}
		});
	}

	/**
	 * Renews the legion's rank and SM_ABYSS_RANKING_LEGIONS
	 */
	public void renewLegionRanking() {
		Map<Integer, Integer> newLegionRankingCache = new HashMap<Integer, Integer>();
		ArrayList<AbyssRankingResult> elyosRanking = getDAO().getAbyssRankingLegions(Race.ELYOS), asmoRanking = getDAO().getAbyssRankingLegions(Race.ASMODIANS);

		legions.clear();
		legions.put(Race.ASMODIANS, new SM_ABYSS_RANKING_LEGIONS(lastUpdate, asmoRanking, Race.ASMODIANS));
		legions.put(Race.ELYOS, new SM_ABYSS_RANKING_LEGIONS(lastUpdate, elyosRanking, Race.ELYOS));

		for (AbyssRankingResult result : elyosRanking) {
			newLegionRankingCache.put(result.getLegionId(), result.getRankPos());
		}
		for (AbyssRankingResult result : asmoRanking) {
			newLegionRankingCache.put(result.getLegionId(), result.getRankPos());
		}

		LegionService.getInstance().performRankingUpdate(newLegionRankingCache);
	}

	/**
	 * Renews the player ranking by race
	 *
	 * @param race
	 */
	private void renewPlayerRanking(Race race) {
		//delete not before new list is created
		List<SM_ABYSS_RANKING_PLAYERS> newlyCalculated;
		newlyCalculated = generatePacketsForRace(race);
		players.remove(race);
		players.put(race, newlyCalculated);
	}

	private List<SM_ABYSS_RANKING_PLAYERS> generatePacketsForRace(Race race) {
		//players orderd by ap
		ArrayList<AbyssRankingResult> list = getDAO().getAbyssRankingPlayers(race, RankingConfig.TOP_RANKING_MAX_OFFLINE_DAYS);
		int page = 1;
		List<SM_ABYSS_RANKING_PLAYERS> playerPackets = new ArrayList<SM_ABYSS_RANKING_PLAYERS>();
		for (int i = 0; i < list.size(); i += 44) {
			if (list.size() > i + 44) {
				playerPackets.add(new SM_ABYSS_RANKING_PLAYERS(lastUpdate, list.subList(i, i + 44), race, page, false));
			}
			else {
				playerPackets.add(new SM_ABYSS_RANKING_PLAYERS(lastUpdate, list.subList(i, list.size()), race, page, true));
			}
			page++;
		}
		return playerPackets;
	}

	/**
	 * @return all players
	 */
	public List<SM_ABYSS_RANKING_PLAYERS> getPlayers(Race race) {
		return players.get(race);
	}

	/**
	 * @return all legions
	 */
	public SM_ABYSS_RANKING_LEGIONS getLegions(Race race) {
		return legions.get(race);
	}

	/**
	 * @return the lastUpdate
	 */
	public int getLastUpdate() {
		return lastUpdate;
	}

	private AbyssRankDAO getDAO() {
		return DAOManager.getDAO(AbyssRankDAO.class);
	}

	public static final AbyssRankingCache getInstance() {
		return SingletonHolder.INSTANCE;
	}

	private static class SingletonHolder {

		protected static final AbyssRankingCache INSTANCE = new AbyssRankingCache();
	}
}
