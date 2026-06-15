package com.aionemu.gameserver.services;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.database.DatabaseFactory;
import com.aionemu.gameserver.configs.main.CleaningConfig;
import com.aionemu.gameserver.dao.LegionDAO;
import com.aionemu.gameserver.dao.LegionMemberDAO;
import com.aionemu.gameserver.dao.PlayerDAO;
import com.aionemu.gameserver.dataholders.PlayerExperienceTable;
import com.aionemu.gameserver.model.team.legion.LegionHistoryAction;
import com.aionemu.gameserver.model.team.legion.LegionRank;
import com.aionemu.gameserver.services.player.PlayerService;
import com.aionemu.gameserver.utils.xml.JAXBUtil;

/**
 * Offers the functionality to delete all data about inactive players
 *
 * @author nrg, Neon
 */
public class DatabaseCleaningService {

	private static final Logger log = LoggerFactory.getLogger(DatabaseCleaningService.class);

	private DatabaseCleaningService() {
	}

	public static void deletePlayersOnInactiveAccounts() {
		if (Thread.currentThread().threadId() != 1)
			throw new IllegalStateException("DatabaseCleaningService can only be run from the main thread on server startup");

		if (CleaningConfig.MIN_ACCOUNT_INACTIVITY_DAYS <= 30)
			throw new IllegalArgumentException("The configured days for database cleaning is too low. For safety reasons the service will only execute with periods over 30 days");

		List<PlayerDAO.PlayerAndLegionInfo> players = PlayerDAO.getPlayersOnInactiveAccounts(toMaxExp(CleaningConfig.MAX_DELETABLE_CHAR_LEVEL), CleaningConfig.MIN_ACCOUNT_INACTIVITY_DAYS);
		if (players.isEmpty()) {
			log.info("Found no inactive accounts with characters level <={} to delete", CleaningConfig.MAX_DELETABLE_CHAR_LEVEL);
			return;
		}
		deletePlayers(players);
		List<PlayerDAO.PlayerAndLegionInfo> remainingLegionMembers = deleteEmptyLegions(players);
		maintainBrigadeGenerals(remainingLegionMembers);
		addLegionHistoryLeaveEntry(remainingLegionMembers);
		if (players.size() >= 500)
			optimizeDatabaseTables(withForeignKeyTables("players", "inventory"));
	}

	private static long toMaxExp(int charLevel) {
		File xml = new File("./data/static_data/player_experience_table.xml"); // fast unmarshal to avoid loading whole static data before
		PlayerExperienceTable pxt = JAXBUtil.deserialize(xml, PlayerExperienceTable.class, "./data/static_data/static_data.xsd");
		return pxt.getStartExpForLevel(charLevel + 1) - 1;
	}

	private static void deletePlayers(List<PlayerDAO.PlayerAndLegionInfo> players) {
		long startMillis = System.currentTimeMillis();
		log.info("Deleting {} characters level <={} from inactive accounts...", players.size(), CleaningConfig.MAX_DELETABLE_CHAR_LEVEL);
		for (int i = 0; i < players.size(); i++) {
			if (i % 20 == 0)
				System.out.printf("Progress: %4.1f%%\r", i * 100f / players.size());
			PlayerService.deletePlayerFromDB(players.get(i).playerId(), false);
		}
		log.info("Deleted characters and related data from database in {} seconds", (System.currentTimeMillis() - startMillis) / 1000);
	}

	private static List<PlayerDAO.PlayerAndLegionInfo> deleteEmptyLegions(List<PlayerDAO.PlayerAndLegionInfo> players) {
		List<PlayerDAO.PlayerAndLegionInfo> remainingLegionMembers = new ArrayList<>();
		Set<Integer> deleted = new HashSet<>();
		for (PlayerDAO.PlayerAndLegionInfo player : players) {
			if (player.legionId() == 0 || deleted.contains(player.legionId()))
				continue;
			if (LegionMemberDAO.loadLegionMembers(player.legionId()).isEmpty()) {
				LegionService.deleteLegionFromDB(player.legionId());
				deleted.add(player.legionId());
			} else {
				remainingLegionMembers.add(player);
			}
		}
		if (!deleted.isEmpty())
			log.info("Deleted {} empty legions", deleted.size());
		return remainingLegionMembers;
	}

	private static void maintainBrigadeGenerals(List<PlayerDAO.PlayerAndLegionInfo> deletedLegionMembers) {
		for (PlayerDAO.PlayerAndLegionInfo deletedLegionMember : deletedLegionMembers) {
			if (deletedLegionMember.legionRank() != LegionRank.BRIGADE_GENERAL)
				continue;
			List<Integer> legionMembers = LegionMemberDAO.loadLegionMembers(deletedLegionMember.legionId());
			if (legionMembers.isEmpty() || legionMembers.contains(deletedLegionMember.playerId()))
				continue;
			int newBrigadeGeneralId = legionMembers.size() == 1 ? legionMembers.getFirst() : 0;
			if (newBrigadeGeneralId != 0 && LegionMemberDAO.setRank(newBrigadeGeneralId, LegionRank.BRIGADE_GENERAL)) {
				String newBrigadeGeneralName = PlayerDAO.getPlayerNameByObjId(newBrigadeGeneralId);
				log.info("Transferred brigade general of legion {} from deleted player {} to the only remaining member {}", deletedLegionMember.legionId(), deletedLegionMember.name(), newBrigadeGeneralName);
				LegionDAO.insertHistory(deletedLegionMember.legionId(), LegionHistoryAction.APPOINTED, newBrigadeGeneralName, "");
			} else {
				log.warn("Legion {} has no brigade general anymore", deletedLegionMember.legionId());
			}
		}
	}

	private static void addLegionHistoryLeaveEntry(List<PlayerDAO.PlayerAndLegionInfo> players) {
		for (PlayerDAO.PlayerAndLegionInfo player : players)
			LegionDAO.insertHistory(player.legionId(), LegionHistoryAction.KICK, player.name(), "");
	}

	@SuppressWarnings("SqlSourceToSinkFlow") // table names cannot be used as parameters, so unfortunately we have to concat the sql query
	private static void optimizeDatabaseTables(List<String> tables) {
		long startMillis = System.currentTimeMillis();
		log.info("Optimizing {} database tables: {}", tables.size(), String.join(", ", tables));
		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement stmt = con.prepareStatement("OPTIMIZE TABLE " + String.join(",", tables))) {
			stmt.execute();
			log.info("Optimized database tables in {} seconds", (System.currentTimeMillis() - startMillis) / 1000);
		} catch (Exception e) {
			log.error("Optimize table failed", e);
		}
	}

	/**
	 * @return baseTables plus all tables which have a foreign key referencing the primary key of one of the given baseTables
	 */
	private static List<String> withForeignKeyTables(String... baseTables) {
		Set<String> tables = new LinkedHashSet<>();
		for (String table : baseTables) {
			tables.add(table);
			try (Connection con = DatabaseFactory.getConnection()) {
				ResultSet importedKeys = con.getMetaData().getExportedKeys(con.getCatalog(), null, table);
				while (importedKeys.next())
					tables.add(importedKeys.getString("FKTABLE_NAME"));
			} catch (Exception e) {
				log.error("Failed to collect tables on players table", e);
			}
		}
		return new ArrayList<>(tables);
	}
}
