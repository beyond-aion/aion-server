package com.aionemu.gameserver.model.templates.abyss;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlList;

import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.utils.stats.AbyssRankEnum;

/**
 * Rewards, limits and requirements of one abyss rank, as seen by the killing player.
 */
@XmlAccessorType(XmlAccessType.NONE)
public class AbyssRankTemplate {

	private static final int RANK_DIFF_OFFSET = 17;
	private static final int DEFAULT_RANK_DIFF_MOD = 100;

	@XmlAttribute(name = "race")
	private Race race;

	@XmlAttribute(name = "rank")
	private AbyssRankEnum rank;

	@XmlAttribute(name = "drop_point")
	private long dropPoint;

	@XmlAttribute(name = "death_penalty_point")
	private long deathPenaltyPoint;

	@XmlAttribute(name = "require_point")
	private long requirePoint;

	@XmlAttribute(name = "require_point_gp")
	private int requirePointGp;

	@XmlAttribute(name = "require_order")
	private int requireOrder;

	@XmlAttribute(name = "give_max_per_user")
	private long giveMaxPerUser;

	@XmlAttribute(name = "delay_time")
	private int delayTime;

	@XmlAttribute(name = "max_from_all_users")
	private long maxFromAllUsers;

	@XmlAttribute(name = "max_reduce_amount")
	private long maxReduceAmount;

	@XmlAttribute(name = "max_reduce_interval")
	private int maxReduceInterval;

	@XmlAttribute(name = "daily_reduce_gp")
	private int dailyReduceGp;

	@XmlAttribute(name = "notify_die")
	private boolean notifyDie;

	@XmlList
	@XmlAttribute(name = "rank_diff_mod")
	private int[] rankDiffMod;

	@XmlList
	@XmlAttribute(name = "ranker_skills")
	private int[] rankerSkills;

	public int getId() {
		return rank.getId();
	}

	public AbyssRankEnum getRankEnum() {
		return rank;
	}

	public Race getRace() {
		return race;
	}

	public int getRank() {
		return rank.getId();
	}

	/**
	 * @return Base abyss points for killing a player of this rank.
	 */
	public long getDropPoint() {
		return dropPoint;
	}

	/**
	 * @return Abyss points the defeated player loses, relative to what the killers gained.
	 */
	public long getDeathPenaltyPoint() {
		return deathPenaltyPoint;
	}

	public long getRequirePoint() {
		return requirePoint;
	}

	public int getRequirePointGp() {
		return requirePointGp;
	}

	/**
	 * @return Maximum position in the GP ranking that this rank may occupy; only officer ranks use it.
	 */
	public int getRequireOrder() {
		return requireOrder;
	}

	public long getGiveMaxPerUser() {
		return giveMaxPerUser;
	}

	/**
	 * @return Seconds during which AP from the same victim keeps accumulating instead of starting over.
	 */
	public int getDelayTime() {
		return delayTime;
	}

	/**
	 * @return Cap of the accumulated AP gained from players.
	 */
	public long getMaxFromAllUsers() {
		return maxFromAllUsers;
	}

	public long getMaxReduceAmount() {
		return maxReduceAmount;
	}

	public int getMaxReduceInterval() {
		return maxReduceInterval;
	}

	/**
	 * @return Glory points lost per day by holders of this rank; 0 disables the loss.
	 */
	public int getDailyReduceGp() {
		return dailyReduceGp;
	}

	/**
	 * @return Whether the death of a holder of this rank is announced in the abyss.
	 */
	public boolean isNotifyDie() {
		return notifyDie;
	}

	/**
	 * @return Skill ids this rank grants to the player.
	 */
	public int[] getRankerSkills() {
		return rankerSkills == null ? new int[0] : rankerSkills;
	}

	/**
	 * @param victimRank
	 *          Rank of the defeated player.
	 * @return Percent modifier of the AP gain against that rank.
	 */
	/**
	 * @param victimRankId
	 *          Abyss rank id of the killed player or NPC, 0 if it has none.
	 * @return The AP modifier in percent for a kill of the given rank by a player of this rank.
	 */
	public int getRankDiffMod(int victimRankId) {
		if (rankDiffMod == null || rankDiffMod.length == 0)
			return DEFAULT_RANK_DIFF_MOD;
		return rankDiffMod[Math.clamp(victimRankId - getId() + RANK_DIFF_OFFSET, 0, rankDiffMod.length - 1)];
	}

	/**
	 * @return The AP for a kill of the given rank after the rank difference modifier, rounded half up like retail.
	 */
	public int applyRankDiffMod(int ap, int victimRankId) {
		long scaled = (long) ap * getRankDiffMod(victimRankId);
		return (int) (scaled / 100 + (scaled % 100 >= 50 ? 1 : 0));
	}
}
