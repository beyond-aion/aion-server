package com.aionemu.gameserver.dataholders;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;

/**
 * Pins the level 60 rows of the two energy tables the live tests use for accumulation: the per-minute salvation gain and its check cap, and
 * the repose tick and its maximum.
 */
class EnergyTableTest {

	@Test
	void testSalvationAtLevelSixty() throws Exception {
		SalvationEnergyTable table = unmarshal(SalvationEnergyTable.class, "salvation_energy_table.xml");
		assertEquals(6550, table.getStandardPlayTime(60));
		assertEquals(655, table.getCheckStartTime(60));
		assertEquals(4852, table.getBonusPointsPerMinute(60));
		assertEquals(5, table.getBonusInterval(60));
		assertEquals(60, table.getCheckInterval(60));
		assertEquals(30, table.getUseRatio(60));
		assertEquals(5, table.getDropRatio(60));
		// the first check and every bonus interval give 24 260, one check interval caps the pool at 291 120
		assertEquals(24260, table.getBonusPointsPerMinute(60) * table.getBonusInterval(60));
		assertEquals(291120, table.getBonusPointsPerMinute(60) * table.getCheckInterval(60));
	}

	@Test
	void testReposeAtLevelSixty() throws Exception {
		ReposeEnergyTable table = unmarshal(ReposeEnergyTable.class, "repose_energy_table.xml");
		assertEquals(10546, table.getEnergyPerTick(60));
		assertEquals(40, table.getUseRatio(60));
		assertEquals(3037248, table.getMaxEnergy(60));
		// six ten-minute ticks per hour, 48 hours to the cap
		assertEquals(63276, 6 * table.getEnergyPerTick(60));
		assertEquals(288, table.getMaxEnergy(60) / table.getEnergyPerTick(60));
	}

	private static <T> T unmarshal(Class<T> type, String file) throws JAXBException {
		Path path = Path.of("data", "static_data", "exp_mod_table", file);
		assertTrue(Files.exists(path), path.toString());
		return type.cast(JAXBContext.newInstance(type).createUnmarshaller().unmarshal(path.toFile()));
	}
}
