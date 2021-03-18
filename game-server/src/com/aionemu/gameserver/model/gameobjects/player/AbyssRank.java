package com.aionemu.gameserver.model.gameobjects.player;

import java.time.ZonedDateTime;
import java.time.temporal.IsoFields;

import com.aionemu.gameserver.configs.main.CustomConfig;
import com.aionemu.gameserver.model.gameobjects.Persistable;
import com.aionemu.gameserver.utils.stats.AbyssRankEnum;
import com.aionemu.gameserver.utils.time.ServerTime;

/**
 * @author ATracer, Divinity
 */
public class AbyssRank implements Persistable {

	private int dailyAP;
	private int weeklyAP;
	private int currentAp;
	private int lastAP;
	private int dailyGP;
	private int weeklyGP;
	private int currentGp;
	private int lastGP;
	private AbyssRankEnum rank;
	private PersistentState persistentState;
	private int dailyKill;
	private int weeklyKill;
	private int allKill;
	private int maxRank;
	private int lastKill;
	private long lastUpdate;

	public AbyssRank(int dailyAP, int weeklyAP, int ap, int rank, int dailyKill, int weeklyKill, int allKill, int maxRank, int lastKill, int lastAP,
		long lastUpdate, int daily_gp, int weekly_gp, int gp, int last_gp) {
		this.dailyAP = dailyAP;
		this.weeklyAP = weeklyAP;
		this.currentAp = ap;
		this.rank = AbyssRankEnum.getRankById(rank);
		this.dailyKill = dailyKill;
		this.weeklyKill = weeklyKill;
		this.allKill = allKill;
		this.maxRank = maxRank;
		this.lastKill = lastKill;
		this.lastAP = lastAP;
		this.lastUpdate = lastUpdate;
		this.dailyGP = daily_gp;
		this.weeklyGP = weekly_gp;
		this.currentGp = gp > 0 ? gp : 0;
		this.lastGP = last_gp;

		doUpdate();
	}

	public enum AbyssRankUpdateType {
		PLAYER_ELYOS(1),
		PLAYER_ASMODIANS(2),
		LEGION_ELYOS(4),
		LEGION_ASMODIANS(8);

		private int id;

		AbyssRankUpdateType(int id) {
			this.id = id;
		}

		public int value() {
			return id;
		}
	}

	/**
	 * Add AP to a player (current player AP + added AP)
	 *
	 * @param additionalAp
	 */
	public void addAp(int additionalAp) {
		if (additionalAp > 0) {
			dailyAP += additionalAp;
			if (dailyAP < 0)
				dailyAP = 0;

			weeklyAP += additionalAp;
			if (weeklyAP < 0)
				weeklyAP = 0;
		}

		int cappedCount;
		if (CustomConfig.ENABLE_AP_CAP)
			cappedCount = currentAp + additionalAp > CustomConfig.AP_CAP_VALUE ? (int) (CustomConfig.AP_CAP_VALUE - currentAp) : additionalAp;
		else
			cappedCount = additionalAp;

		currentAp += cappedCount;
		if (currentAp < 0)
			currentAp = 0;

		AbyssRankEnum newRank = AbyssRankEnum.getRankForPoints(currentAp, currentGp);
		if (newRank.getRequiredGP() == 0)
			setRank(newRank);
		setPersistentState(PersistentState.UPDATE_REQUIRED);
	}

	/**
	 * Do not use this method directly. Add GP by using {@link com.aionemu.gameserver.services.abyss.GloryPointsService}
	 * @param amount Amount of GloryPoints to add
	 * @param addToStats true, if daily and weekly gp stats should be modified
	 */
	public void increaseGp(int amount, boolean addToStats) {
		if (addToStats) {
			dailyGP += amount;
			weeklyGP += amount;
		}
		currentGp += amount;
		setPersistentState(PersistentState.UPDATE_REQUIRED);
	}

	/**
	 * Do not use this method directly. Remove GP by using {@link com.aionemu.gameserver.services.abyss.GloryPointsService}
	 * @param amount Amount of GloryPoints to remove
	 */
	public void reduceGp(int amount) {
		currentGp -= amount;
		if (currentGp < 0)
			currentGp = 0;
		setPersistentState(PersistentState.UPDATE_REQUIRED);
	}

	/**
	 * @return The daily Abyss Pointn count
	 */
	public int getDailyAP() {
		return dailyAP;
	}

	/**
	 * @return The weekly Abyss Point count
	 */
	public int getWeeklyAP() {
		return weeklyAP;
	}

	/**
	 * @return The all time Abyss Point count
	 */
	public int getAp() {
		return currentAp;
	}

	/**
	 * @return the rank
	 */
	public AbyssRankEnum getRank() {
		return rank;
	}

	public void setRank(AbyssRankEnum rank) {
		if (rank.getId() > this.maxRank)
			this.maxRank = rank.getId();

		this.rank = rank;
		setPersistentState(PersistentState.UPDATE_REQUIRED);
	}

	/**
	 * @return The daily count kill
	 */
	public int getDailyKill() {
		return dailyKill;
	}

	/**
	 * @return The weekly count kill
	 */
	public int getWeeklyKill() {
		return weeklyKill;
	}

	/**
	 * @return all Kill
	 */
	public int getAllKill() {
		return allKill;
	}

	/**
	 * Add one kill to a player
	 */
	public void incrementAllKills() {
		dailyKill++;
		weeklyKill++;
		allKill++;
	}

	/**
	 * @return max Rank
	 */
	public int getMaxRank() {
		return maxRank;
	}

	/**
	 * @return The last week count kill
	 */
	public int getLastKill() {
		return lastKill;
	}

	/**
	 * @return The last week Abyss Point count
	 */
	public int getLastAP() {
		return lastAP;
	}

	/**
	 * @return the persistentState
	 */
	@Override
	public PersistentState getPersistentState() {
		return persistentState;
	}

	/**
	 * @param persistentState
	 *          the persistentState to set
	 */
	@Override
	public void setPersistentState(PersistentState persistentState) {
		if (persistentState != PersistentState.UPDATE_REQUIRED || this.persistentState != PersistentState.NEW)
			this.persistentState = persistentState;
	}

	/**
	 * @return The last update of the AbyssRank
	 */
	public long getLastUpdate() {
		return lastUpdate;
	}

	/**
	 * Make an update for the daily/weekly/last kill & ap counts
	 */
	public void doUpdate() {
		boolean needUpdate = false;
		ZonedDateTime lastTime = ServerTime.ofEpochMilli(lastUpdate);
		ZonedDateTime now = ServerTime.now();

		// Checking the day - month & year are checked to prevent if a player come back after 1 month, the same day
		if (lastTime.getDayOfMonth() != now.getDayOfMonth() || lastTime.getMonth() != now.getMonth() || lastTime.getYear() != now.getYear()) {
			dailyAP = 0;
			dailyKill = 0;
			dailyGP = 0;
			needUpdate = true;
		}

		// Checking the week - year is checked to prevent if a player come back after 1 year, the same week
		if (lastTime.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR) != now.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR) || lastTime.getYear() != now.getYear()) {
			lastKill = weeklyKill;
			lastAP = weeklyAP;
			lastGP = weeklyGP;
			weeklyKill = 0;
			weeklyAP = 0;
			weeklyGP = 0;
			needUpdate = true;
		}

		// For offline changed ranks
		if (rank.getId() > maxRank) {
			maxRank = rank.getId();
			needUpdate = true;
		}

		// Finally, update the the last update
		lastUpdate = System.currentTimeMillis();

		if (needUpdate)
			setPersistentState(PersistentState.UPDATE_REQUIRED);
	}

	public int getDailyGP() {
		return dailyGP;
	}

	public int getWeeklyGP() {
		return weeklyGP;
	}

	public int getCurrentGP() {
		return currentGp;
	}

	public int getLastGP() {
		return lastGP;
	}
}
