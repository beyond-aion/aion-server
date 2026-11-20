package com.aionemu.gameserver.utils.stats;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.StringReader;

import org.junit.jupiter.api.Test;

import com.aionemu.gameserver.dataholders.AbyssRankTable;
import com.aionemu.gameserver.dataholders.PcExpModTable;
import com.aionemu.gameserver.dataholders.PvpModTable;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.templates.npc.FirstAttackSplitRate;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;

/**
 * Pins the reward split and the level difference tables, so that changes to them are deliberate.
 */
class NpcRewardTest {

	@Test
	void testExpSplitLikeTheNpcServer() {
		FirstAttackSplitRate split = FirstAttackSplitRate.Split_Default;
		// who dealt all damage gets the whole base, without a split
		assertEquals(1000, StatFunctions.calculateExpShare(1000, split, 7, 7, false));
		assertEquals(450, StatFunctions.calculateExpShare(1000, split, 1, 2, false));
		assertEquals(550, StatFunctions.calculateExpShare(1000, split, 1, 2, true));
		// 284500 (23 214 539 XP, Split_Little): 0.95f * base in float, the damage share in double; all float gives 17 152 966
		assertEquals(17_152_964, StatFunctions.calculateExpShare(23_214_539, FirstAttackSplitRate.Split_Little, 7, 9, false));
	}

	@Test
	void testExpSplitVariants() {
		assertEquals(300, StatFunctions.calculateExpShare(1000, FirstAttackSplitRate.Split_None, 3, 10, false));
		assertEquals(475, StatFunctions.calculateExpShare(1000, FirstAttackSplitRate.Split_Little, 1, 2, false));
		assertEquals(375, StatFunctions.calculateExpShare(1000, FirstAttackSplitRate.Split_Much, 1, 2, false));
		assertEquals(245, StatFunctions.calculateExpShare(1000, FirstAttackSplitRate.Split_Extra, 1, 2, false));
		assertEquals(510, StatFunctions.calculateExpShare(1000, FirstAttackSplitRate.Split_Extra, 0, 1, true));
	}

	@Test
	void testRewardShareIsTruncatedOnce() {
		// DP, AP and GP keep the share fractional until their own multiplier is applied; there is no whole-base shortcut as for XP
		assertEquals(64.5f, StatFunctions.calculateRewardShare(65, FirstAttackSplitRate.Split_Default, 1, 1, true), 0.001f);
		assertEquals(26.3f, StatFunctions.calculateRewardShare(27, FirstAttackSplitRate.Split_Default, 1, 1, true), 0.001f);
		assertEquals(88692.7f, StatFunctions.calculateRewardShare(88693, FirstAttackSplitRate.Split_Default, 1, 1, true), 0.01f);
		// a team gets the share truncated, AP and DP at least one
		assertEquals(26, StatFunctions.calculateTeamPool(27, FirstAttackSplitRate.Split_Default, 1, 1, true, true));
		assertEquals(1, StatFunctions.calculateTeamPool(27, FirstAttackSplitRate.Split_Default, 0, 1, false, true));
		assertEquals(0, StatFunctions.calculateTeamPool(27, FirstAttackSplitRate.Split_Default, 0, 1, false, false));
		assertEquals(0, StatFunctions.calculateTeamPool(0, FirstAttackSplitRate.Split_Default, 1, 1, true, true));
	}

	@Test
	void testDpLevelFactor() {
		assertEquals(0, StatFunctions.getDPLevelFactor(9), 0.001f);
		assertEquals(1, StatFunctions.getDPLevelFactor(10), 0.001f);
		assertEquals(2.1f, StatFunctions.getDPLevelFactor(65), 0.001f);
	}

	@Test
	void testPcExpModUsesFiveLevelColumns() throws JAXBException {
		PcExpModTable table = unmarshal(PcExpModTable.class, table("pcexp_mod_table", 20));
		// level 65 is its own column, not the shared 61..69 one
		assertEquals(0.13f, table.getMultiplier(65, 64), 0.001f);
		assertEquals(0.13f, table.getMultiplier(65, 10), 0.001f);
		// below the first row the first row is used
		assertEquals(0.02f, table.getMultiplier(10, 65), 0.001f);
	}

	@Test
	void testPvpModUsesTenLevelColumns() throws JAXBException {
		PvpModTable table = unmarshal(PvpModTable.class, table("pvp_mod_table", 10));
		assertEquals(0.07f, table.getMultiplier(65, 64), 0.001f);
		assertEquals(0.07f, table.getMultiplier(65, 10), 0.001f);
	}

	@Test
	void testPvpDpShare() {
		assertEquals(500, StatFunctions.calculateRewardShare(StatFunctions.getPvpDpBase(), FirstAttackSplitRate.Split_Default, 1, 1, true), 0.001f);
		assertEquals(225, StatFunctions.calculateRewardShare(StatFunctions.getPvpDpBase(), FirstAttackSplitRate.Split_Default, 1, 2, false), 0.001f);
		assertEquals(50, StatFunctions.calculateRewardShare(StatFunctions.getPvpDpBase(), FirstAttackSplitRate.Split_Default, 0, 1, true), 0.001f);
	}

	@Test
	void testAbyssRankTable() throws JAXBException {
		AbyssRankTable table = unmarshal(AbyssRankTable.class, rankTable());
		assertEquals(2, table.size());
		assertEquals(300, table.get(Race.ELYOS, 1).getDropPoint());
		assertEquals(414, table.get(Race.ELYOS, 2).getDropPoint());
		assertEquals(90, table.get(Race.ELYOS, 1).getDeathPenaltyPoint());
		assertEquals(21600, table.get(Race.ELYOS, 1).getDelayTime());
		assertEquals(600, table.get(Race.ELYOS, 1).getGiveMaxPerUser());
		assertEquals(15000, table.get(Race.ELYOS, 1).getMaxFromAllUsers());
		assertEquals(2000, table.get(Race.ELYOS, 1).getMaxReduceAmount());
		assertEquals(300, table.get(Race.ELYOS, 1).getMaxReduceInterval());
		// the rank difference (victim - killer) is shifted by 17 and clamped to the 35 entries
		assertEquals(17, table.get(Race.ELYOS, 1).getRankDiffMod(1));
		assertEquals(0, table.get(Race.ELYOS, 1).getRankDiffMod(-29));
		assertEquals(34, table.get(Race.ELYOS, 1).getRankDiffMod(31));
	}

	private static String rankTable() {
		StringBuilder diffs = new StringBuilder();
		for (int i = 0; i < 35; i++)
			diffs.append(i == 0 ? "" : " ").append(i);
		return "<?xml version=\"1.0\" encoding=\"UTF-8\"?><abyss_ranks>"
			+ "<abyss_rank race=\"ELYOS\" rank=\"GRADE9_SOLDIER\" drop_point=\"300\" death_penalty_point=\"90\""
			+ " delay_time=\"21600\" give_max_per_user=\"600\" max_from_all_users=\"15000\""
			+ " max_reduce_amount=\"2000\" max_reduce_interval=\"300\""
			+ " rank_diff_mod=\"" + diffs + "\"/>"
			+ "<abyss_rank race=\"ELYOS\" rank=\"GRADE8_SOLDIER\" drop_point=\"414\" death_penalty_point=\"103\" rank_diff_mod=\""
			+ diffs + "\"/>"
			+ "</abyss_ranks>";
	}

	/**
	 * @return Table XML with one row per level difference and 1..columns as values
	 */
	private static String table(String name, int columns) {
		StringBuilder values = new StringBuilder();
		for (int i = 1; i <= columns; i++)
			values.append(i == 1 ? "" : " ").append(i);
		StringBuilder xml = new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\"?><" + name + ">");
		for (int diff = -25; diff <= 24; diff++)
			xml.append("<level_diff_mod level_diff=\"").append(diff).append("\" mods=\"").append(values).append("\"/>");
		return xml.append("</").append(name).append(">").toString();
	}

	private static <T> T unmarshal(Class<T> type, String xml) throws JAXBException {
		return type.cast(JAXBContext.newInstance(type).createUnmarshaller().unmarshal(new StringReader(xml)));
	}
}
