package com.aionemu.gameserver.dao;

import java.util.List;
import java.util.Map;

import com.aionemu.commons.database.dao.DAO;
import com.aionemu.gameserver.model.Gender;
import com.aionemu.gameserver.model.PlayerClass;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.gameobjects.player.AbyssRank;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.utils.stats.AbyssRankEnum;

/**
 * @author ATracer
 */
public abstract class AbyssRankDAO implements DAO {

	@Override
	public final String getClassName() {
		return AbyssRankDAO.class.getName();
	}

	public abstract void loadAbyssRank(Player player);

	public abstract AbyssRank loadAbyssRank(int playerId);

	public abstract boolean storeAbyssRank(Player player);

	public abstract List<RankingListPlayer> loadRankingListPlayers();

	public abstract List<RankingListPlayerGp> loadRankingListPlayersGp(Race race);

	public abstract List<RankingListLegion> loadRankingListLegions();

	public abstract Map<Integer, Integer> loadApOfPlayersNotInRankingList(Race race, AbyssRankEnum minRank);

	public abstract void updateAbyssRank(int playerId, AbyssRankEnum rankEnum);

	public abstract void updateRankingLists(int maxOfflineDays, int playerLimit, int legionLimit);

	public abstract void dailyUpdateGp(AbyssRankEnum rank);

	public abstract void increaseGp(int playerObjId, int additionalGp);

	public abstract void decreaseGp(int playerObjId, int gpToRemove);

	public record RankingListPlayerGp(int position, int playerId, int gp) {
	}

	public record RankingListPlayer(int position, int oldPosition, int id, String name, Race race, int level, int abyssRank, int ap, int gp, int title,
		PlayerClass playerClass, Gender gender, String legionName) {
	}

	public record RankingListLegion(int position, int oldPosition, int id, String name, Race race, int level, long contributionPoints, int memberCount) {
	}
}
