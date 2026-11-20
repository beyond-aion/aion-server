package com.aionemu.gameserver.dataholders;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.xml.XMLConstants;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import jakarta.xml.bind.JAXBContext;

import org.junit.jupiter.api.Test;

import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.templates.abyss.AbyssRankTemplate;
import com.aionemu.gameserver.utils.stats.AbyssRankEnum;

/**
 * Pins the shipped abyss rank table and its new data-driven rank requirements.
 */
class AbyssRankTableTest {

	@Test
	void loadsTheShippedRankTable() throws Exception {
		AbyssRankTable table = load();
		assertEquals(36, table.size());

		AbyssRankTemplate rank1 = table.get(Race.ELYOS, 1);
		assertEquals(1, rank1.getId());
		assertEquals(300, rank1.getDropPoint());
		assertEquals(90, rank1.getDeathPenaltyPoint());
		assertEquals(0, rank1.getRequirePointGp());
		assertEquals(0, rank1.getRequireOrder());
		assertEquals(0, rank1.getDailyReduceGp());
		assertFalse(rank1.isNotifyDie());
		assertEquals(100, rank1.getRankDiffMod(0));
		assertEquals(100, rank1.getRankDiffMod(AbyssRankEnum.GRADE1_SOLDIER.getId()));
		assertEquals(100, rank1.getRankDiffMod(AbyssRankEnum.SUPREME_COMMANDER.getId()));
		// 4.8 has no fortress AP cap
		assertEquals(15000, rank1.getMaxFromAllUsers());

		AbyssRankTemplate rank9 = table.get(Race.ELYOS, 9);
		assertEquals(1245, rank9.getDropPoint());
		assertEquals(150800, rank9.getRequirePoint());
		assertEquals(20000, rank9.getMaxFromAllUsers());

		AbyssRankTemplate rank10 = table.get(Race.ELYOS, 10);
		assertEquals(1868, rank10.getDropPoint());
		assertEquals(1244, rank10.getRequirePointGp());
		assertEquals(1000, rank10.getRequireOrder());
		assertEquals(7, rank10.getDailyReduceGp());
		assertFalse(rank10.isNotifyDie());

		AbyssRankTemplate rank18 = table.get(Race.ELYOS, 18);
		assertEquals(7099, rank18.getDropPoint());
		assertEquals(12437, rank18.getRequirePointGp());
		assertEquals(1, rank18.getRequireOrder());
		assertEquals(147, rank18.getDailyReduceGp());
		assertTrue(rank18.isNotifyDie());
	}

	@Test
	void loadsRankerSkills() throws Exception {
		AbyssRankTable table = load();
		assertArrayEquals(new int[0], table.get(Race.ELYOS, 13).getRankerSkills());
		assertArrayEquals(new int[] { 11885, 11895 }, table.get(Race.ELYOS, 14).getRankerSkills());
		assertArrayEquals(new int[] { 11889, 11898, 11900, 11903, 11904, 11905, 11906 },
			table.get(Race.ELYOS, 18).getRankerSkills());
		assertArrayEquals(new int[] { 11894, 11898, 11902, 11903, 11904, 11905, 11906 },
			table.get(Race.ASMODIANS, 18).getRankerSkills());
	}

	@Test
	void assignsRanksFromTheTable() throws Exception {
		AbyssRankTable table = load();
		assertEquals(9, table.getRankForAp(Race.ELYOS, 150_800).getRank());
		assertEquals(8, table.getRankForAp(Race.ELYOS, 150_799).getRank());
		assertEquals(1, table.getRankForAp(Race.ELYOS, 0).getRank());
		assertEquals(10, table.getMinGpRank(Race.ELYOS).getRank());
		assertEquals(1000, table.getMaxRequireOrder(Race.ELYOS));
		// the dormant GP loss selects the rank by GP alone
		assertEquals(10, table.getRankForGp(Race.ELYOS, 1244).getRank());
		assertNull(table.getRankForGp(Race.ELYOS, 1243));
		assertEquals(11, table.getRankForGp(Race.ELYOS, 1368).getRank());
		assertEquals(15, table.getRankForGp(Race.ELYOS, 8335).getRank());
		assertEquals(18, table.getRankForGp(Race.ELYOS, 12437).getRank());
		assertNull(table.getRankForGp(Race.ELYOS, 0));
	}

	@Test
	void assignsRanksFromPositionAndPoints() throws Exception {
		AbyssRankTable table = load();
		// no ranking position -> officer ranks are invalid
		assertEquals(9, table.getRankForPointsAndPosition(Race.ELYOS, 150_800, 1244, 0).getRank());
		// STAR1 requires the top 1000
		assertEquals(10, table.getRankForPointsAndPosition(Race.ELYOS, 150_800, 1244, 1000).getRank());
		assertEquals(9, table.getRankForPointsAndPosition(Race.ELYOS, 150_800, 1244, 1001).getRank());
		// STAR2 requires the top 700, otherwise STAR1 still fits
		assertEquals(11, table.getRankForPointsAndPosition(Race.ELYOS, 150_800, 1368, 700).getRank());
		assertEquals(10, table.getRankForPointsAndPosition(Race.ELYOS, 150_800, 1368, 701).getRank());
		// below the STAR1 GP threshold even the best position does not help
		assertEquals(9, table.getRankForPointsAndPosition(Race.ELYOS, 150_800, 1243, 1000).getRank());
		// GENERAL requires the top 30, STAR5 still fits a worse position
		assertEquals(15, table.getRankForPointsAndPosition(Race.ELYOS, 150_800, 8335, 30).getRank());
		assertEquals(14, table.getRankForPointsAndPosition(Race.ELYOS, 150_800, 8335, 31).getRank());
	}

	@Test
	void revalidatesLikeRetail() throws Exception {
		AbyssRankTable table = load();
		// AP ranks follow the AP in both directions, GP alone never makes an officer
		assertEquals(9, table.revalidate(Race.ELYOS, AbyssRankEnum.GRADE5_SOLDIER, 150_800, 5000).getRank());
		assertEquals(5, table.revalidate(Race.ELYOS, AbyssRankEnum.GRADE1_SOLDIER, 23_500, 5000).getRank());
		// an officer keeps the rank of the daily update while the GP reaches the first officer rank, even below the own requirement
		assertEquals(15, table.revalidate(Race.ELYOS, AbyssRankEnum.GENERAL, 0, 1244).getRank());
		assertEquals(10, table.revalidate(Race.ELYOS, AbyssRankEnum.STAR1_OFFICER, 0, 20_000).getRank());
		// below it the officer rank is lost at once and the AP decides
		assertEquals(9, table.revalidate(Race.ELYOS, AbyssRankEnum.GENERAL, 150_800, 1243).getRank());
		assertEquals(1, table.revalidate(Race.ELYOS, AbyssRankEnum.STAR1_OFFICER, 0, 1243).getRank());
	}

	private static AbyssRankTable load() throws Exception {
		Path xml = resolve("data/static_data/abyss/abyss_rank.xml");
		Path xsd = resolve("data/static_data/abyss/abyss_rank.xsd");
		Schema schema = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI).newSchema(xsd.toFile());
		var unmarshaller = JAXBContext.newInstance(AbyssRankTable.class).createUnmarshaller();
		unmarshaller.setSchema(schema);
		return (AbyssRankTable) unmarshaller.unmarshal(xml.toFile());
	}

	private static Path resolve(String relative) {
		Path path = Paths.get(relative);
		return Files.exists(path) ? path : Paths.get("game-server", relative);
	}
}
