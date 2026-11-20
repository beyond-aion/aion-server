package com.aionemu.gameserver.utils.stats;

import jakarta.xml.bind.annotation.XmlEnum;

import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.utils.ChatUtil;

/**
 * Identity and client localisation of the abyss ranks. All numbers live in {@code abyss_rank.xml}.
 */
@XmlEnum
public enum AbyssRankEnum {
	GRADE9_SOLDIER(1),
	GRADE8_SOLDIER(2),
	GRADE7_SOLDIER(3),
	GRADE6_SOLDIER(4),
	GRADE5_SOLDIER(5),
	GRADE4_SOLDIER(6),
	GRADE3_SOLDIER(7),
	GRADE2_SOLDIER(8),
	GRADE1_SOLDIER(9),
	STAR1_OFFICER(10),
	STAR2_OFFICER(11),
	STAR3_OFFICER(12),
	STAR4_OFFICER(13),
	STAR5_OFFICER(14),
	GENERAL(15),
	GREAT_GENERAL(16),
	COMMANDER(17),
	SUPREME_COMMANDER(18);

	private final int id;

	AbyssRankEnum(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
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
}
