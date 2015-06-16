package com.aionemu.gameserver.utils.stats;

import javax.xml.bind.annotation.XmlEnum;

import com.aionemu.gameserver.configs.main.RateConfig;
import com.aionemu.gameserver.model.DescriptionId;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.gameobjects.player.Player;

/**
 * @author ATracer
 * @author Sarynth
 * @author Imaginary
 */
@XmlEnum
public enum AbyssRankEnum {
	GRADE9_SOLDIER(1, 300, 90, 0, 0, 1802431, 0, 0),
	GRADE8_SOLDIER(2, 345, 103, 1200, 0, 1802433, 0, 0),
	GRADE7_SOLDIER(3, 396, 118, 4220, 0, 1802435, 0, 0),
	GRADE6_SOLDIER(4, 455, 136, 10990, 0, 1802437, 0, 0),
	GRADE5_SOLDIER(5, 523, 156, 23500, 0, 1802439, 0, 0),
	GRADE4_SOLDIER(6, 601, 180, 42780, 0, 1802441, 0, 0),
	GRADE3_SOLDIER(7, 721, 216, 69700, 0, 1802443, 0, 0),
	GRADE2_SOLDIER(8, 865, 259, 105600, 0, 1802445, 0, 0),
	GRADE1_SOLDIER(9, 1038, 311, 150800, 0, 1802447, 0, 0),
	STAR1_OFFICER(10, 1557, 467, 0, 1000, 1802449, 1244, 9),
	STAR2_OFFICER(11, 1868, 560, 0, 700, 1802451, 1368, 19),
	STAR3_OFFICER(12, 2148, 644, 0, 500, 1802453, 1915, 31),
	STAR4_OFFICER(13, 2470, 741, 0, 300, 1802455, 3064, 51),
	STAR5_OFFICER(14, 3705, 1482, 0, 100, 1802457, 5210, 150),
	GENERAL(15, 4075, 1630, 0, 30, 1802459, 8335, 171),
	GREAT_GENERAL(16, 4482, 1792, 0, 10, 1802461, 10002, 176),
	COMMANDER(17, 4930, 1972, 0, 3, 1802463, 11503, 190),
	SUPREME_COMMANDER(18, 5916, 2366, 0, 1, 1802465, 12437, 219);

	private int id;
	private int pointsGained;
	private int pointsLost;
	private int requiredAP;
	private int quota;
	private int descriptionId;
	private int requiredGP;
	private int gpLossPerDay;

	/**
	 * @param id
	 * @param pointsGained
	 * @param pointsLost
	 * @param required
	 * @param quota
	 */
	private AbyssRankEnum(int id, int pointsGained, int pointsLost, int required, int quota, int descriptionId, int gloryPointsRequired, int gpLossPerDay) {
		this.id = id;
		this.pointsGained = pointsGained;
		this.pointsLost = pointsLost;
		this.requiredAP = required * RateConfig.ABYSS_RANK_RATE;
		this.quota = quota;
		this.descriptionId = descriptionId;
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
		return gpLossPerDay;
	}

	/**
	 * @return The quota is the maximum number of allowed player to have the rank
	 */
	public int getQuota() {
		return quota;
	}
	
	public int getDescriptionId() {
		return descriptionId;
	}

	public static DescriptionId getRankDescriptionId(Player player){
		int pRankId = player.getAbyssRank().getRank().getId();
		for (AbyssRankEnum rank : values()) {
			if (rank.getId() == pRankId) {
				int descId = rank.getDescriptionId();
				return (player.getRace() == Race.ELYOS) ? new DescriptionId(descId) : new DescriptionId(descId + 36);
			}
		}
		throw new IllegalArgumentException("No rank Description Id found for player: " + player);
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
		throw new IllegalArgumentException("Invalid abyss rank provided" + id);
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
