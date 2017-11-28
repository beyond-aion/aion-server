package com.aionemu.gameserver.utils.stats;

import javax.xml.bind.annotation.XmlEnum;

import com.aionemu.gameserver.configs.main.RankingConfig;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.utils.ChatUtil;

/**
 * @author ATracer
 * @author Sarynth
 * @author Imaginary
 */
@XmlEnum
public enum AbyssRankEnum {
	GRADE9_SOLDIER(1, 300, 90, 0, 0, 0, 0),
	GRADE8_SOLDIER(2, 345, 103, 1200, 0, 0, 0),
	GRADE7_SOLDIER(3, 396, 118, 4220, 0, 0, 0),
	GRADE6_SOLDIER(4, 455, 136, 10990, 0, 0, 0),
	GRADE5_SOLDIER(5, 523, 156, 23500, 0, 0, 0),
	GRADE4_SOLDIER(6, 601, 180, 42780, 0, 0, 0),
	GRADE3_SOLDIER(7, 721, 216, 69700, 0, 0, 0),
	GRADE2_SOLDIER(8, 865, 259, 105600, 0, 0, 0),
	GRADE1_SOLDIER(9, 1038, 311, 150800, 0, 0, 0),
	STAR1_OFFICER(10, 1557, 467, 0, 1000, 1244, 14),
	STAR2_OFFICER(11, 1868, 560, 0, 700, 1368, 29),
	STAR3_OFFICER(12, 2148, 644, 0, 500, 1915, 57),
	STAR4_OFFICER(13, 2470, 741, 0, 300, 3064, 107),
	STAR5_OFFICER(14, 3705, 1482, 0, 100, 5210, 179),
	GENERAL(15, 4075, 1630, 0, 30, 8335, 357),
	GREAT_GENERAL(16, 4482, 1792, 0, 10, 10002, 464),
	COMMANDER(17, 4930, 1972, 0, 3, 11503, 571),
	SUPREME_COMMANDER(18, 5916, 2366, 0, 1, 12437, 714);

	private final int id;
	private final int pointsGained;
	private final int pointsLost;
	private final int requiredAP;
	private final int quota;
	private final int requiredGP;
	private final int gpLossPerDay;

	/**
	 * @param id
	 * @param pointsGained
	 * @param pointsLost
	 * @param required
	 * @param quota
	 */
	private AbyssRankEnum(int id, int pointsGained, int pointsLost, int required, int quota, int gloryPointsRequired, int gpLossPerDay) {
		this.id = id;
		this.pointsGained = pointsGained;
		this.pointsLost = pointsLost;
		this.requiredAP = required;
		this.quota = quota;
		this.requiredGP = gloryPointsRequired;
		this.gpLossPerDay = gpLossPerDay;
	}

	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}

	/**
	 * @return the pointsLost
	 */
	public int getPointsLost() {
		return pointsLost;
	}

	/**
	 * @return the pointsGained
	 */
	public int getPointsGained() {
		return pointsGained;
	}

	/**
	 * @return AP required for Rank
	 */
	public int getRequiredAP() {
		return requiredAP;
	}

	public int getRequiredGP() {
		return requiredGP;
	}

	public int getGpLossPerDay() {
		int gpLossCap = RankingConfig.TOP_RANKING_GP_LOSS_CAP;
		return gpLossCap > -1 && gpLossPerDay > gpLossCap ? gpLossCap : gpLossPerDay;
	}

	/**
	 * @return The quota is the maximum number of allowed player to have the rank
	 */
	public int getQuota() {
		return quota;
	}

	public static String getRankL10n(Player player) {
		return player.getAbyssRank().getRank().getRankL10n(player.getRace());
	}

	public static String getRankL10n(Race race, int rankId) {
		return getRankById(rankId).getRankL10n(race);
	}

	public String getRankL10n(Race race) {
		int rank9L10nId = race == Race.ELYOS ? 901215 : 901233;
		int rankL10nId = rank9L10nId + ordinal();
		return ChatUtil.l10n(rankL10nId);
	}

	/**
	 * @param id
	 * @return The abyss rank enum by his id
	 */
	public static AbyssRankEnum getRankById(int id) {
		for (AbyssRankEnum rank : values()) {
			if (rank.getId() == id)
				return rank;
		}
		throw new IllegalArgumentException("Invalid abyss rank provided " + id);
	}

	/**
	 * @param ap
	 * @return The abyss rank enum for his needed ap
	 */
	public static AbyssRankEnum getRankForPoints(int ap, int gp) {
		AbyssRankEnum r = AbyssRankEnum.GRADE9_SOLDIER;
		for (AbyssRankEnum rank : values()) {
			if (rank.getRequiredAP() <= ap && rank.getRequiredGP() <= gp)
				r = rank;
		}
		return r;
	}
}
