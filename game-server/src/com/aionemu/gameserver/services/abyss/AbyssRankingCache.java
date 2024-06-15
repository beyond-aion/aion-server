package com.aionemu.gameserver.services.abyss;

import java.util.*;
import java.util.stream.Collectors;

import com.aionemu.gameserver.dao.AbyssRankDAO;
import com.aionemu.gameserver.dao.AbyssRankDAO.RankingListLegion;
import com.aionemu.gameserver.dao.AbyssRankDAO.RankingListPlayer;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.team.legion.Legion;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ABYSS_RANKING_LEGIONS;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ABYSS_RANKING_PLAYERS;
import com.aionemu.gameserver.network.aion.serverpackets.SM_LEGION_EDIT;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.world.World;

/**
 * @author VladimirZ, Neon
 */
public class AbyssRankingCache {

	private Map<Integer, RankingListPlayer> rankingListPlayers;
	private Map<Integer, RankingListLegion> rankingListLegions;
	/**
	 * Player ranking list that will show up in the abyss ranking window
	 */
	private Map<Race, List<SM_ABYSS_RANKING_PLAYERS>> playerRankListPackets;

	/**
	 * Legion ranking list that will show up in the abyss ranking window
	 */
	private Map<Race, SM_ABYSS_RANKING_LEGIONS> legionRankListPackets;

	/**
	 * Last update time that will show up in the abyss ranking window
	 */
	private int lastUpdate;

	private AbyssRankingCache() {
		refreshCache();
	}

	public static AbyssRankingCache getInstance() {
		return SingletonHolder.INSTANCE;
	}

	/**
	 * Loads ranking data from DB
	 */
	private void refreshCache() {
		List<Race> races = Arrays.asList(Race.ASMODIANS, Race.ELYOS);
		List<RankingListPlayer> rankingListPlayers = AbyssRankDAO.loadRankingListPlayers();
		List<RankingListLegion> rankingListLegions = AbyssRankDAO.loadRankingListLegions();
		Map<Race, List<SM_ABYSS_RANKING_PLAYERS>> newPlayerRankListPackets = new HashMap<>();
		Map<Race, SM_ABYSS_RANKING_LEGIONS> newLegionRankListPackets = new HashMap<>();

		int updateTime = (int) (System.currentTimeMillis() / 1000);

		for (Race race : races) {
			List<RankingListPlayer> players = rankingListPlayers.stream().filter(p -> p.race() == race).collect(Collectors.toList());
			newPlayerRankListPackets.put(race, getPlayerRankListPackets(updateTime, race, players));

			List<RankingListLegion> legions = rankingListLegions.stream().filter(l -> l.race() == race).collect(Collectors.toList());
			newLegionRankListPackets.put(race, new SM_ABYSS_RANKING_LEGIONS(updateTime, legions, race));
		}

		// assign the finished lists
		this.rankingListPlayers = rankingListPlayers.stream().collect(Collectors.toMap(RankingListPlayer::id, p -> p));
		this.rankingListLegions = rankingListLegions.stream().collect(Collectors.toMap(RankingListLegion::id, l -> l));
		this.playerRankListPackets = newPlayerRankListPackets;
		this.legionRankListPackets = newLegionRankListPackets;
		this.lastUpdate = updateTime;
	}

	/**
	 * Reloads player & legion rank data from DB and refreshes the in-game views
	 */
	public void reloadRankings() {
		// update cache
		refreshCache();

		World.getInstance().forEachPlayer(player -> {
			player.resetAbyssRankListUpdated();
			if (player.getLegion() != null) // update legion rank number
				PacketSendUtility.sendPacket(player, new SM_LEGION_EDIT(0x01, player.getLegion()));
		});
	}

	private List<SM_ABYSS_RANKING_PLAYERS> getPlayerRankListPackets(int updateTime, Race race, List<RankingListPlayer> list) {
		List<SM_ABYSS_RANKING_PLAYERS> playerPackets = new ArrayList<>();
		int page = 1;

		for (int i = 0; i < list.size(); i += 44) {
			if (list.size() > i + 44) {
				playerPackets.add(new SM_ABYSS_RANKING_PLAYERS(updateTime, list.subList(i, i + 44), race, page, false));
			} else {
				playerPackets.add(new SM_ABYSS_RANKING_PLAYERS(updateTime, list.subList(i, list.size()), race, page, true));
			}
			page++;
		}

		return playerPackets;
	}

	public List<SM_ABYSS_RANKING_PLAYERS> getPlayers(Race race) {
		return playerRankListPackets.get(race);
	}

	public SM_ABYSS_RANKING_LEGIONS getLegions(Race race) {
		return legionRankListPackets.get(race);
	}

	public int getRankingListPosition(Player player) {
		RankingListPlayer rank = rankingListPlayers.get(player.getObjectId());
		return rank == null ? 0 : rank.position();
	}

	public int getRankingListPosition(Legion legion) {
		RankingListLegion rank = rankingListLegions.get(legion.getLegionId());
		return rank == null ? 0 : rank.position();
	}

	public int getLastUpdate() {
		return lastUpdate;
	}

	private static class SingletonHolder {

		protected static final AbyssRankingCache INSTANCE = new AbyssRankingCache();
	}
}
