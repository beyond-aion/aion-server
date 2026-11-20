package com.aionemu.gameserver.utils.stats;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.StringReader;
import java.lang.reflect.Field;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.aionemu.gameserver.dataholders.AbyssPointWorldModTable;
import com.aionemu.gameserver.dataholders.AbyssRankTable;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.dataholders.PvpModTable;
import com.aionemu.gameserver.configs.main.GSConfig;
import com.aionemu.gameserver.configs.main.RatesConfig;
import com.aionemu.gameserver.model.PlayerClass;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.account.Account;
import com.aionemu.gameserver.model.account.PlayerAccountData;
import com.aionemu.gameserver.model.gameobjects.player.AbyssRank;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.player.PlayerAppearance;
import com.aionemu.gameserver.model.gameobjects.player.PlayerCommonData;
import com.aionemu.gameserver.model.templates.npc.FirstAttackSplitRate;
import com.aionemu.gameserver.world.WorldPosition;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;

class RewardHarnessTest {

	@BeforeAll
	static void initTables() throws JAXBException {
		GSConfig.TIME_ZONE_ID = java.time.ZoneId.of("UTC");
		RatesConfig.AP_PVP_LOSS_RATES = new float[] { 1f };
		DataManager.PVP_MOD_TABLE = unmarshal(PvpModTable.class, pvpMods());
		DataManager.ABYSS_RANK_TABLE = unmarshal(AbyssRankTable.class, ranks());
		DataManager.ABYSS_POINT_WORLD_MOD_TABLE = new AbyssPointWorldModTable();
	}

	@Test
	void testPvpDpReward() {
		Player killer = player(1, "D", 64, Race.ELYOS, 9);
		Player victim = player(2, "C", 64, Race.ASMODIANS, 1);
		float part = StatFunctions.calculateRewardShare(StatFunctions.getPvpDpBase(), FirstAttackSplitRate.Split_Default, 1, 1, true);
		assertEquals(1040, StatFunctions.calculatePvpDpReward(killer, victim, part));
	}

	@Test
	void testPvpApBaseAndReward() {
		Player killer = player(1, "D", 64, Race.ELYOS, 9);
		Player victim = player(2, "C", 64, Race.ASMODIANS, 1);
		assertEquals(300, StatFunctions.calculatePvpApBase(victim, 1f));
		assertEquals(600, StatFunctions.calculatePvpApBase(victim, 2f));
		float part = StatFunctions.calculateRewardShare(300, FirstAttackSplitRate.Split_Default, 1, 1, true);
		assertEquals(300, StatFunctions.calculatePvpApReward(killer, victim, part));
	}

	@Test
	void testVictimRankChangesTheBase() {
		Player killer = player(1, "D", 64, Race.ELYOS, 9);
		Player highVictim = player(3, "C2", 64, Race.ASMODIANS, 9);
		assertEquals(1245, StatFunctions.calculatePvpApBase(highVictim, 1f));
		// 0.9 * 1245 + (int) (0.1 * 1245) = 1120.5 + 124, the first-attacker part is truncated as retail does
		float part = StatFunctions.calculateRewardShare(1245, FirstAttackSplitRate.Split_Default, 1, 1, true);
		assertEquals(1244.5f, part, 0.001f);
		assertEquals(1244, StatFunctions.calculatePvpApReward(killer, highVictim, part));
	}

	@Test
	void testPvpApLossFollowsTheVictimRank() {
		Player lowVictim = player(2, "C", 64, Race.ASMODIANS, 1);
		Player highVictim = player(3, "C2", 64, Race.ASMODIANS, 9);
		assertEquals(90, StatFunctions.calculatePvPApLoss(lowVictim, 300, 1f));
		assertEquals(311, StatFunctions.calculatePvPApLoss(highVictim, 1245, 1f));
		// half of the kill costs half of the loss; the pool multiplier of the killers does not raise it
		assertEquals(45, StatFunctions.calculatePvPApLoss(lowVictim, 150, 1f));
		assertEquals(90, StatFunctions.calculatePvPApLoss(lowVictim, 600, 2f));
	}

	private static Player player(int id, String name, int level, Race race, int rank) {
		PlayerCommonData pcd = new PlayerCommonData(id);
		pcd.setName(name);
		pcd.setRace(race);
		pcd.setPlayerClass(PlayerClass.GLADIATOR);
		set(pcd, "level", level);
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

	private static String pvpMods() {
		StringBuilder xml = new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\"?><pvp_mod_table>");
		for (int diff = -25; diff <= 24; diff++)
			xml.append("<level_diff_mod level_diff=\"").append(diff).append("\" mods=\"100 100 100 100 100 100 100 100 100 100\"/>");
		return xml.append("</pvp_mod_table>").toString();
	}

	private static String ranks() {
		String mods = String.join(" ", java.util.Collections.nCopies(35, "100"));
		return "<?xml version=\"1.0\" encoding=\"UTF-8\"?><abyss_ranks>"
			+ "<abyss_rank race=\"ELYOS\" rank=\"GRADE9_SOLDIER\" drop_point=\"300\" death_penalty_point=\"90\" rank_diff_mod=\"" + mods + "\"/>"
			+ "<abyss_rank race=\"ELYOS\" rank=\"GRADE1_SOLDIER\" drop_point=\"1245\" death_penalty_point=\"311\" rank_diff_mod=\"" + mods + "\"/>"
			+ "<abyss_rank race=\"ASMODIANS\" rank=\"GRADE9_SOLDIER\" drop_point=\"300\" death_penalty_point=\"90\" rank_diff_mod=\"" + mods + "\"/>"
			+ "<abyss_rank race=\"ASMODIANS\" rank=\"GRADE1_SOLDIER\" drop_point=\"1245\" death_penalty_point=\"311\" rank_diff_mod=\"" + mods + "\"/>"
			+ "</abyss_ranks>";
	}

	private static <T> T unmarshal(Class<T> type, String xml) throws JAXBException {
		return type.cast(JAXBContext.newInstance(type).createUnmarshaller().unmarshal(new StringReader(xml)));
	}
}
