package com.aionemu.gameserver.dao;

import java.util.List;
import java.util.Map;

import com.aionemu.commons.database.dao.DAO;
import com.aionemu.gameserver.model.AbyssRankingResult;
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

	public abstract List<AbyssRankingResult> getAbyssRankingPlayers(Race race, final int maxOfflineDays);

	public abstract List<AbyssRankingResult> getAbyssRankingLegions(Race race);

	public abstract Map<Integer, Integer[]> loadPlayersGpAp(Race race, final AbyssRankEnum limitRank, final int maxOfflineDays);

	public abstract void updateAbyssRank(int playerId, AbyssRankEnum rankEnum);

	public abstract void updateRankList(final int maxOfflineDays);

	public abstract void dailyUpdateGp(AbyssRankEnum rank);

	public abstract void increaseGp(int playerObjId, int additionalGp);

	public abstract void decreaseGp(int playerObjId, int gpToRemove);
}
