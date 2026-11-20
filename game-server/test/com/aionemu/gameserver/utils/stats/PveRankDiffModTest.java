package com.aionemu.gameserver.utils.stats;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.StringReader;
import java.util.Arrays;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.aionemu.gameserver.dataholders.AbyssRankTable;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.templates.abyss.AbyssRankTemplate;

import jakarta.xml.bind.JAXBContext;

/**
 * Pins the rank difference modifier that retail applies to every AP gain, including the NPC ones.
 */
class PveRankDiffModTest {

	private static AbyssRankTemplate killerRank;

	@BeforeAll
	static void loadRanks() throws Exception {
		killerRank = unmarshal(ranks()).get(Race.ELYOS, 9);
	}

	@Test
	void appliesTheVictimRankDifference() {
		assertEquals(200, killerRank.applyRankDiffMod(100, 9)); // same rank
		assertEquals(100, killerRank.applyRankDiffMod(100, 1)); // eight ranks below
		assertEquals(100, killerRank.applyRankDiffMod(100, 0)); // an NPC without a rank
		assertEquals(0, killerRank.applyRankDiffMod(0, 9));
	}

	@Test
	void roundsHalfUpLikeRetail() {
		assertEquals(5, killerRank.applyRankDiffMod(3, 10)); // 3 * 150% = 4.5
		assertEquals(6, killerRank.applyRankDiffMod(5, 8)); // 5 * 110% = 5.5
		assertEquals(4, killerRank.applyRankDiffMod(4, 8)); // 4 * 110% = 4.4
	}

	private static String ranks() {
		String[] mods = new String[35];
		Arrays.fill(mods, "100");
		mods[16] = "110"; // rank difference -1
		mods[17] = "200"; // rank difference 0
		mods[18] = "150"; // rank difference 1
		String table = String.join(" ", mods);
		return "<?xml version=\"1.0\" encoding=\"UTF-8\"?><abyss_ranks>"
			+ "<abyss_rank race=\"ELYOS\" rank=\"GRADE9_SOLDIER\" drop_point=\"300\" death_penalty_point=\"90\" rank_diff_mod=\"" + table + "\"/>"
			+ "<abyss_rank race=\"ELYOS\" rank=\"GRADE1_SOLDIER\" drop_point=\"1245\" death_penalty_point=\"311\" rank_diff_mod=\"" + table + "\"/>"
			+ "</abyss_ranks>";
	}

	private static AbyssRankTable unmarshal(String xml) throws Exception {
		var unmarshaller = JAXBContext.newInstance(AbyssRankTable.class).createUnmarshaller();
		return (AbyssRankTable) unmarshaller.unmarshal(new StringReader(xml));
	}
}
