package com.aionemu.gameserver.utils.stats;

import javax.xml.bind.annotation.XmlEnum;

import com.aionemu.gameserver.configs.main.RankingConfig;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.utils.ChatUtil;

/**
 * @author ATracer, Sarynth, Imaginary
 */
@XmlEnum
public enum AbyssRankEnum {
	GRADE9_SOLDIER(1, 300, 90, 0, 0),
	GRADE8_SOLDIER(2, 345, 103, 1200, 0),
	GRADE7_SOLDIER(3, 396, 118, 4220, 0),
	GRADE6_SOLDIER(4, 455, 136, 10990, 0),
	GRADE5_SOLDIER(5, 523, 156, 23500, 0),
	GRADE4_SOLDIER(6, 601, 180, 42780, 0),
	GRADE3_SOLDIER(7, 721, 216, 69700, 0),
	GRADE2_SOLDIER(8, 865, 259, 105600, 0),
	GRADE1_SOLDIER(9, 1038, 311, 150800, 0),
	STAR1_OFFICER(10, 1557, 467, 0, 1244),
	STAR2_OFFICER(11, 1868, 560, 0, 1368),
	STAR3_OFFICER(12, 2148, 644, 0, 1915),
	STAR4_OFFICER(13, 2470, 741, 0, 3064),
	STAR5_OFFICER(14, 3705, 1482, 0, 5210),
	GENERAL(15, 4075, 1630, 0, 8335),
	GREAT_GENERAL(16, 4482, 1792, 0, 10002),
	COMMANDER(17, 4930, 1972, 0, 11503),
	SUPREME_COMMANDER(18, 5916, 2366, 0, 12437);

	private final int id;
	private final int pointsGained;
	private final int pointsLost;
	private final int requiredAP;
	private final int requiredGP;

	AbyssRankEnum(int id, int pointsGained, int pointsLost, int required, int gloryPointsRequired) {
		this.id = id;
		this.pointsGained = pointsGained;
		this.pointsLost = pointsLost;
		this.requiredAP = required;
		this.requiredGP = gloryPointsRequired;
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
		return RankingConfig.TOP_RANKING_GP_LOSS.getOrDefault(this, 0);
	}

	/**
	 * @return The quota is the maximum number of allowed player to have the rank
	 */
	public int getQuota() {
		return RankingConfig.TOP_RANKING_QUOTA.getOrDefault(this, 0);
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

	public static AbyssRankEnum getRankById(int id) {
		for (AbyssRankEnum rank : values()) {
			if (rank.getId() == id)
				return rank;
		}
		throw new IllegalArgumentException("Invalid abyss rank provided " + id);
	}

	public static AbyssRankEnum getRankForPoints(int ap, int gp) {
		AbyssRankEnum r = AbyssRankEnum.GRADE9_SOLDIER;
		for (AbyssRankEnum rank : values()) {
			if (rank.getRequiredAP() <= ap && rank.getRequiredGP() <= gp)
				r = rank;
		}
		return r;
	}
}
