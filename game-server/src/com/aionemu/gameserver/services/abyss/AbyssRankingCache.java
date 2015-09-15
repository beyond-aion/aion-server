package com.aionemu.gameserver.services.abyss;

import java.util.Arrays;
import java.util.List;

import javolution.util.FastMap;
import javolution.util.FastTable;

import com.aionemu.commons.database.dao.DAOManager;
import com.aionemu.gameserver.configs.main.RankingConfig;
import com.aionemu.gameserver.dao.AbyssRankDAO;
import com.aionemu.gameserver.model.AbyssRankingResult;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.team.legion.Legion;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ABYSS_RANKING_LEGIONS;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ABYSS_RANKING_PLAYERS;
import com.aionemu.gameserver.network.aion.serverpackets.SM_LEGION_EDIT;
import com.aionemu.gameserver.services.LegionService;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.world.World;
import com.aionemu.gameserver.world.knownlist.Visitor;

/**
 * @author VladimirZ
 * @modified Neon
 */
public class AbyssRankingCache {

	/**
	 * Player ranking list that will show up in the abyss ranking window
	 */
	private FastMap<Race, List<SM_ABYSS_RANKING_PLAYERS>> playerRankListPackets;

	/**
	 * Legion ranking list that will show up in the abyss ranking window
	 */
	private FastMap<Race, SM_ABYSS_RANKING_LEGIONS> legionRankListPackets;

	/**
	 * Legion ranking map for legion initialization
	 */
	private FastMap<Integer, Integer> legionRanking;

	/**
	 * Last update time that will show up in the abyss ranking window
	 */
	private int lastUpdate;

	private AbyssRankingCache() {
		refreshCache();
	}

	public static final AbyssRankingCache getInstance() {
		return SingletonHolder.INSTANCE;
	}

	/**
	 * Loads ranking data from DB
	 */
	private void refreshCache() {
		List<Race> races = Arrays.asList(Race.ASMODIANS, Race.ELYOS);
		FastMap<Race, List<SM_ABYSS_RANKING_PLAYERS>> newPlayerRankListPackets = new FastMap<Race, List<SM_ABYSS_RANKING_PLAYERS>>();
		FastMap<Race, SM_ABYSS_RANKING_LEGIONS> newLegionRankListPackets = new FastMap<Race, SM_ABYSS_RANKING_LEGIONS>();
		FastMap<Integer, Integer> newLegionRanking = new FastMap<Integer, Integer>();
		List<AbyssRankingResult> rankList;

		int updateTime = (int) (System.currentTimeMillis() / 1000);

		for (Race race : races) {
			// load player ranks
			rankList = getDAO().getAbyssRankingPlayers(race, RankingConfig.TOP_RANKING_MAX_OFFLINE_DAYS); // players ordered by GP
			newPlayerRankListPackets.put(race, getPlayerRankListPackets(race, rankList));

			// load legion ranks
			rankList = getDAO().getAbyssRankingLegions(race); // legions ordered by contribution points
			newLegionRankListPackets.put(race, new SM_ABYSS_RANKING_LEGIONS(updateTime, rankList, race));
			newLegionRanking.putAll(getLegionRanking(rankList));
		}

		// assign the finished lists
		this.playerRankListPackets = newPlayerRankListPackets;
		this.legionRankListPackets = newLegionRankListPackets;
		this.legionRanking = newLegionRanking;
		this.lastUpdate = updateTime;
	}

	/**
	 * Reloads player & legion rank data from DB and refreshes the in-game views
	 */
	public void reloadRankings() {
		// update cache
		refreshCache();

		// notify online clients
		updateAbyssRankList();
		updateLegionRankingList();
	}

	/**
	 * Updates the ranking list for all online players
	 */
	public void updateAbyssRankList() {
		World.getInstance().doOnAllPlayers(new Visitor<Player>() {

			@Override
			public void visit(Player player) {
				player.resetAbyssRankListUpdated();
			}
		});
	}

	/**
	 * Updates the ranking information for all cached legions
	 */
	public void updateLegionRankingList() {
		for (Legion legion : LegionService.getInstance().getCachedLegions()) {
			if (legionRanking.containsKey(legion.getLegionId())) {
				legion.setLegionRank(getLegionRank(legion));
				PacketSendUtility.broadcastPacketToLegion(legion, new SM_LEGION_EDIT(0x01, legion));
			}
		}
	}

	private List<SM_ABYSS_RANKING_PLAYERS> getPlayerRankListPackets(Race race, List<AbyssRankingResult> list) {
		List<SM_ABYSS_RANKING_PLAYERS> playerPackets = new FastTable<SM_ABYSS_RANKING_PLAYERS>();
		int page = 1;

		for (int i = 0; i < list.size(); i += 44) {
			if (list.size() > i + 44) {
				playerPackets.add(new SM_ABYSS_RANKING_PLAYERS(lastUpdate, list.subList(i, i + 44), race, page, false));
			} else {
				playerPackets.add(new SM_ABYSS_RANKING_PLAYERS(lastUpdate, list.subList(i, list.size()), race, page, true));
			}
			page++;
		}

		return playerPackets;
	}

	private FastMap<Integer, Integer> getLegionRanking(List<AbyssRankingResult> rankList) {
		FastMap<Integer, Integer> rankMap = new FastMap<Integer, Integer>();

		for (AbyssRankingResult rank : rankList) {
			rankMap.put(rank.getLegionId(), rank.getRankPos());
		}

		return rankMap;
	}

	/**
	 * @return all players
	 */
	public List<SM_ABYSS_RANKING_PLAYERS> getPlayers(Race race) {
		return playerRankListPackets.get(race);
	}

	/**
	 * @return all legions
	 */
	public SM_ABYSS_RANKING_LEGIONS getLegions(Race race) {
		return legionRankListPackets.get(race);
	}

	/**
	 * @param legion
	 * @return The rank position or 0, if not in ranking cache
	 */
	public int getLegionRank(Legion legion) {
		Integer rankPos = legionRanking.get(legion.getLegionId());
		return rankPos != null ? rankPos : 0;
	}

	/**
	 * @return last ranking update time
	 */
	public int getLastUpdate() {
		return lastUpdate;
	}

	private AbyssRankDAO getDAO() {
		return DAOManager.getDAO(AbyssRankDAO.class);
	}

	private static class SingletonHolder {

		protected static final AbyssRankingCache INSTANCE = new AbyssRankingCache();
	}
}
