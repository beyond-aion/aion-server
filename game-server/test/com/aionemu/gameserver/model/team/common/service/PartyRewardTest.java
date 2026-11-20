package com.aionemu.gameserver.model.team.common.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.StringReader;

import org.junit.jupiter.api.Test;

import com.aionemu.gameserver.dataholders.PartyExpModTable;
import com.aionemu.gameserver.utils.stats.StatFunctions;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;

/**
 * Pins the group and mentor parts of the reward distribution: the weights of the party table and the arithmetic the service applies to the
 * team pool. The live group wiring itself still needs the client.
 */
class PartyRewardTest {

	@Test
	void testPartyWeights() throws JAXBException {
		PartyExpModTable table = unmarshal(table());
		assertEquals(20.0f, table.getWeight(60, 60), 0.001f);
		assertEquals(13.2f, table.getWeight(55, 60), 0.001f);
		assertEquals(20.0f, table.getWeight(64, 64), 0.001f);
		assertEquals(12.0f, table.getWeight(58, 64), 0.001f);
	}

	@Test
	void testMentorPenalty() {
		// 57 501 with one mentor in three: the mentor's third is lost, the two mentees split 38 334
		assertEquals(38334, PlayerTeamDistributionService.applyMentorPenalty(57501, 1, 3));
		assertEquals(57501, PlayerTeamDistributionService.applyMentorPenalty(57501, 0, 3));
	}

	@Test
	void testGroupRewardsOfAgileAbex() {
		float shareA = 20.0f / 33.2f, shareB = 13.2f / 33.2f;
		assertEquals(64115, StatFunctions.calculateTeamMemberXp(88693, shareA, 1.2f));
		assertEquals(42316, StatFunctions.calculateTeamMemberXp(88693, shareB, 1.2f));
		assertEquals(72, StatFunctions.calculateTeamMemberDp(50, shareA, 1.2f, 60));
		assertEquals(45, StatFunctions.calculateTeamMemberDp(50, shareB, 1.2f, 55));
		// the team pool of 26.3 AP is truncated to 26 before it is shared
		assertEquals(18, StatFunctions.calculateTeamMemberAp(26, shareA, 1.2f));
		assertEquals(12, StatFunctions.calculateTeamMemberAp(26, shareB, 1.2f));
	}

	@Test
	void testGroupOfSixtyFourAndFiftyEight() {
		float share64 = 20.0f / 32f, share58 = 12.0f / 32f;
		assertEquals(58204, StatFunctions.calculateTeamMemberXp(88693, share64, 1.05f));
		assertEquals(39911, StatFunctions.calculateTeamMemberXp(88693, share58, 1.2f));
		assertEquals(68, StatFunctions.calculateTeamMemberDp(50, share64, 1.05f, 64));
		assertEquals(44, StatFunctions.calculateTeamMemberDp(50, share58, 1.2f, 58));
	}

	@Test
	void testLiveGroupOn58() {
		// 284500 (dp 900, gp 600) killed by a 63 and a 65 with about 99.9% of the damage: the team pools are truncated to 899 DP and 599 GP,
		// the live values 971/987 DP and 314/313 GP only come out of whole pools (a pool of 900 would give 973 DP)
		float share63 = 1825f / 3825f, share65 = 2000f / 3825f;
		assertEquals(971, StatFunctions.calculateTeamMemberDp(899, share63, 1.1f, 63));
		assertEquals(987, StatFunctions.calculateTeamMemberDp(899, share65, 1.0f, 65));
		assertEquals(314, StatFunctions.calculateTeamMemberGp(599, share63, 1.1f));
		assertEquals(313, StatFunctions.calculateTeamMemberGp(599, share65, 1.0f));
	}

	@Test
	void testHyperionGloryPoints() {
		assertEquals(720, StatFunctions.calculateTeamMemberGp(600, 1f, 1.2f));
		assertEquals(630, StatFunctions.calculateTeamMemberGp(600, 1f, 1.05f));
	}

	private static PartyExpModTable unmarshal(String xml) throws JAXBException {
		return (PartyExpModTable) JAXBContext.newInstance(PartyExpModTable.class).createUnmarshaller().unmarshal(new StringReader(xml));
	}

	private static String table() {
		String[] rows = { "2000 2000 2000 2000 2000 2000 2000 2000", "1715 1900 1900 1900 1900 1900 1900 1900", "1430 1825 1825 1825 1825 1825 1825 1825",
			"1145 1650 1650 1650 1650 1650 1650 1650", "860 1480 1480 1480 1480 1480 1480 1480", "430 1320 1320 1320 1320 1320 1320 1320",
			"215 1200 1200 1200 1200 1200 1200 1200", "107 1020 1020 1020 1020 1020 1020 1020", "53 920 920 920 920 920 920 920",
			"26 860 860 860 860 860 860 860", "21 800 800 800 800 800 800 800", "1 360 360 360 360 360 360 360", "1 60 60 60 60 60 60 60",
			"1 1 1 1 1 1 1 1", "1 1 1 1 1 1 1 1" };
		StringBuilder xml = new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\"?><party_exp_mod_table>");
		for (int diff = 0; diff < rows.length; diff++)
			xml.append("<level_diff_mod level_diff=\"").append(diff).append("\" mods=\"").append(rows[diff]).append("\"/>");
		return xml.append("</party_exp_mod_table>").toString();
	}
}
