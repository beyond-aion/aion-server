package com.aionemu.gameserver.services;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.xml.XMLConstants;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import jakarta.xml.bind.JAXBContext;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.aionemu.gameserver.configs.main.GSConfig;
import com.aionemu.gameserver.dataholders.AbyssRankTable;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.PlayerClass;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.account.Account;
import com.aionemu.gameserver.model.account.PlayerAccountData;
import com.aionemu.gameserver.model.gameobjects.player.AbyssRank;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.player.PlayerAppearance;
import com.aionemu.gameserver.model.gameobjects.player.PlayerCommonData;
import com.aionemu.gameserver.world.WorldPosition;

/**
 * Pins which rank drives each abyss point limit: the accumulated cap belongs to the killer, while the per-victim cap and cooldown belong to the
 * victim.
 */
class AbyssPointLimitServiceTest {

	@BeforeAll
	static void loadRankTable() throws Exception {
		GSConfig.TIME_ZONE_ID = java.time.ZoneId.of("UTC");
		DataManager.ABYSS_RANK_TABLE = load();
	}

	@Test
	void capsTheSameVictimByTheVictimRank() {
		// killer is STAR1 (give_max 5604), victim is GRADE2 (give_max 3114): AP stops after 1244 + 2 * 1037
		Player killer = player(9_000_001, Race.ELYOS, 10);
		Player victim = player(9_000_002, Race.ASMODIANS, 8);
		AbyssPointLimitService service = AbyssPointLimitService.getInstance();
		assertTrue(service.canGainAp(killer, victim, 1244));
		assertTrue(service.canGainAp(killer, victim, 1037));
		assertTrue(service.canGainAp(killer, victim, 1037));
		assertFalse(service.canGainAp(killer, victim, 1037));
	}

	@Test
	void keepsTheAccumulatedCapOnTheKillerRank() {
		// killer is GRADE9 (max_from_all_users 15000), victim is SUPREME (give_max 21297)
		Player killer = player(9_000_003, Race.ELYOS, 1);
		Player victim = player(9_000_004, Race.ASMODIANS, 18);
		AbyssPointLimitService service = AbyssPointLimitService.getInstance();
		assertTrue(service.canGainAp(killer, victim, 14999));
		assertTrue(service.canGainAp(killer, victim, 5000)); // the whole amount is added even above the cap
		assertFalse(service.canGainAp(killer, victim, 1)); // 19999 is already above the killer cap
	}

	@Test
	void limitsNpcKillsOnlyByTheAccumulatedCap() {
		Player killer = player(9_000_005, Race.ELYOS, 10);
		AbyssPointLimitService service = AbyssPointLimitService.getInstance();
		assertTrue(service.canGainAp(killer, 15000));
		assertTrue(service.canGainAp(killer, 15000));
		assertFalse(service.canGainAp(killer, 1)); // 30000 is above the STAR1 cap of 20000
	}

	private static Player player(int objectId, Race race, int rank) {
		PlayerCommonData pcd = new PlayerCommonData(objectId);
		pcd.setName("P" + objectId);
		pcd.setRace(race);
		pcd.setPlayerClass(PlayerClass.GLADIATOR);
		set(pcd, "level", 65);
		set(pcd, "isDaeva", true);
		Player player = new Player(new PlayerAccountData(pcd, new PlayerAppearance()), new Account(1));
		player.setPosition(new WorldPosition(210010000));
		player.setAbyssRank(new AbyssRank(0, 0, 0, rank, 0, 0, 0, rank, 0, 0, 0, 0, 0, 0, 0));
		return player;
	}

	private static void set(Object object, String name, Object value) {
		try {
			Field field = object.getClass().getDeclaredField(name);
			field.setAccessible(true);
			field.set(object, value);
		} catch (ReflectiveOperationException e) {
			throw new IllegalStateException(e);
		}
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
