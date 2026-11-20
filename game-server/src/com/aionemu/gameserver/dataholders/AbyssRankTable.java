package com.aionemu.gameserver.dataholders;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;

import jakarta.xml.bind.Unmarshaller;
import jakarta.xml.bind.annotation.*;

import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.templates.abyss.AbyssRankTemplate;
import com.aionemu.gameserver.utils.stats.AbyssRankEnum;

/**
 * Abyss rank rewards and requirements by race and rank.
 */
@XmlRootElement(name = "abyss_ranks")
@XmlAccessorType(XmlAccessType.NONE)
public class AbyssRankTable {

	private static final AbyssRankTemplate[] EMPTY = new AbyssRankTemplate[2];

	@XmlElement(name = "abyss_rank")
	private List<AbyssRankTemplate> ranks;

	private final Map<Race, AbyssRankTemplate[]> byRace = new HashMap<>();

	@SuppressWarnings("unused")
	private void afterUnmarshal(Unmarshaller unmarshaller, Object parent) {
		Map<Race, Integer> maxRankByRace = new HashMap<>();
		for (AbyssRankTemplate rank : ranks)
			maxRankByRace.merge(rank.getRace(), rank.getRank(), Math::max);
		maxRankByRace.forEach((race, maxRank) -> byRace.put(race, new AbyssRankTemplate[maxRank + 1]));
		for (AbyssRankTemplate rank : ranks)
			byRace.get(rank.getRace())[rank.getRank()] = rank;
	}

	/**
	 * @return Rank template of the given race; the first rank is used when the rank is unknown.
	 */
	public AbyssRankTemplate get(Race race, int rank) {
		AbyssRankTemplate[] table = byRace.get(race);
		if (table == null)
			return null;
		return rank > 0 && rank < table.length && table[rank] != null ? table[rank] : table[1];
	}

	public AbyssRankTemplate get(Race race, AbyssRankEnum rank) {
		return rank == null ? null : get(race, rank.getId());
	}

	/**
	 * @return Highest rank whose AP, GP and ranking position requirements the player meets. Officer ranks additionally need a position within
	 *         {@code require_order}; a missing position (0) therefore only allows AP ranks. This is how the daily update assigns the ranks.
	 */
	public AbyssRankTemplate getRankForPointsAndPosition(Race race, long ap, long gp, int position) {
		return highest(race, table(race)[1], rank -> rank.getRequirePoint() <= ap && rank.getRequirePointGp() <= gp
			&& (rank.getRequirePointGp() == 0 || position > 0 && position <= rank.getRequireOrder()));
	}

	/**
	 * Rank after an AP or GP change or on login. An officer rank comes only from the daily update and stays until the next one, unless the player
	 * no longer meets the requirements of the first officer rank; every other rank follows the AP.
	 *
	 * @return The rank the player has to hold now.
	 */
	public AbyssRankTemplate revalidate(Race race, AbyssRankEnum currentRank, long ap, long gp) {
		AbyssRankTemplate current = get(race, currentRank);
		AbyssRankTemplate firstOfficerRank = getMinGpRank(race);
		if (current != null && current.getRequirePointGp() > 0 && firstOfficerRank != null && gp >= firstOfficerRank.getRequirePointGp()
			&& ap >= firstOfficerRank.getRequirePoint())
			return current;
		return getRankForAp(race, ap);
	}

	/**
	 * @return Highest rank whose GP requirement the player meets, ignoring AP and the ranking position; {@code null} below the first GP rank.
	 *         The dormant GP loss uses this lookup because officer ranks are defined by their GP requirement, not by the current position.
	 */
	public AbyssRankTemplate getRankForGp(Race race, long gp) {
		return highest(race, null, rank -> rank.getRequirePointGp() > 0 && rank.getRequirePointGp() <= gp);
	}

	/**
	 * @return Highest rank that only requires AP.
	 */
	public AbyssRankTemplate getRankForAp(Race race, long ap) {
		return highest(race, table(race)[1], rank -> rank.getRequirePointGp() == 0 && rank.getRequirePoint() <= ap);
	}

	/**
	 * @return First rank that requires glory points.
	 */
	public AbyssRankTemplate getMinGpRank(Race race) {
		for (AbyssRankTemplate rank : table(race))
			if (rank != null && rank.getRequirePointGp() > 0)
				return rank;
		return null;
	}

	/**
	 * @return Largest ranking position used by any rank of the race; it is the number of players the GP list must cover.
	 */
	public int getMaxRequireOrder(Race race) {
		return Arrays.stream(table(race)).filter(Objects::nonNull).mapToInt(AbyssRankTemplate::getRequireOrder).max().orElse(0);
	}

	private AbyssRankTemplate[] table(Race race) {
		return byRace.getOrDefault(race, EMPTY);
	}

	private AbyssRankTemplate highest(Race race, AbyssRankTemplate fallback, Predicate<AbyssRankTemplate> condition) {
		AbyssRankTemplate result = fallback;
		for (AbyssRankTemplate rank : table(race))
			if (rank != null && condition.test(rank))
				result = rank;
		return result;
	}

	public List<AbyssRankTemplate> getRanks() {
		return ranks;
	}

	public int size() {
		return byRace.values().stream().mapToInt(table -> table.length - 1).sum();
	}
}
