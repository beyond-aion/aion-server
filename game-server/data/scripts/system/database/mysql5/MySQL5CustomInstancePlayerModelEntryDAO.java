package mysql5;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.LoggerFactory;

import com.aionemu.commons.database.DB;
import com.aionemu.commons.database.DatabaseFactory;
import com.aionemu.commons.database.ParamReadStH;
import com.aionemu.gameserver.custom.instance.neuralnetwork.PlayerModelEntry;
import com.aionemu.gameserver.dao.CustomInstancePlayerModelEntryDAO;
import com.aionemu.gameserver.dao.MySQL5DAOUtils;
import com.aionemu.gameserver.model.gameobjects.Persistable.PersistentState;

/**
 * @author Jo, Estrayl
 */
public class MySQL5CustomInstancePlayerModelEntryDAO extends CustomInstancePlayerModelEntryDAO {

	private static final String SELECT_QUERY = "SELECT * FROM `custom_instance_records`";
	private static final String INSERT_QUERY = "INSERT INTO `custom_instance_records` ( `player_id`, `timestamp`, `skill_id`, `player_class_id`, `player_hp_percentage`,"
		+ "`player_mp_percentage`, `player_loc_x`, `player_loc_y`, `player_loc_z`, `player_is_rooted`, `player_is_silenced`, `player_is_bound`, `player_is_stunned`, `player_is_aetherhold`,"
		+ "`player_buff_count`, `player_debuff_count`, `player_is_shielded`, `target_id`, `target_class_id`, `target_hp_percentage`, `target_mp_percentage`, `target_is_pvp`,"
		+ "`target_focuses_player`, `distance`, `target_loc_x`, `target_loc_y`, `target_loc_z`, `target_is_rooted`, `target_is_silenced`, `target_is_bound`, `target_is_stunned`,"
		+ "`target_is_aetherhold`, `target_buff_count`, `target_debuff_count`, `target_is_shielded`, `is_boss_phase`) VALUES (?, ?, ?, ?, ?, ?, ?, ?,?, ?, ?, ?, ?, ?, ?, ?, ?, ?, "
		+ "?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

	@Override
	public Map<Integer, List<PlayerModelEntry>> loadPlayerModelEntries() {
		Map<Integer, List<PlayerModelEntry>> playerModelEntries = new ConcurrentHashMap<>();

		DB.select(SELECT_QUERY, new ParamReadStH() {

			@Override
			public void handleRead(ResultSet rset) throws SQLException {
				while (rset.next()) {
					int playerId = rset.getInt("player_id");
					PlayerModelEntry pme = new PlayerModelEntry(playerId, rset.getTimestamp("timestamp"), rset.getInt("skill_id"),
						rset.getInt("player_class_id"), rset.getFloat("player_hp_percentage"), rset.getFloat("player_mp_percentage"),
						rset.getFloat("player_loc_x"), rset.getFloat("player_loc_y"), rset.getFloat("player_loc_z"), rset.getBoolean("player_is_rooted"),
						rset.getBoolean("player_is_silenced"), rset.getBoolean("player_is_bound"), rset.getBoolean("player_is_stunned"),
						rset.getBoolean("player_is_aetherhold"), rset.getInt("player_buff_count"), rset.getInt("player_debuff_count"),
						rset.getBoolean("player_is_shielded"), rset.getInt("target_id"), rset.getFloat("target_hp_percentage"),
						rset.getFloat("target_mp_percentage"), rset.getBoolean("target_is_pvp"), rset.getBoolean("target_focuses_player"),
						rset.getFloat("distance"), rset.getFloat("target_loc_x"), rset.getFloat("target_loc_y"), rset.getFloat("target_loc_z"),
						rset.getBoolean("target_is_rooted"), rset.getBoolean("target_is_silenced"), rset.getBoolean("target_is_bound"),
						rset.getBoolean("target_is_stunned"), rset.getBoolean("target_is_aetherhold"), rset.getInt("target_buff_count"),
						rset.getInt("target_debuff_count"), rset.getBoolean("target_is_shielded"), rset.getBoolean("is_boss_phase"));

					List<PlayerModelEntry> entries = playerModelEntries.get(playerId);
					if (entries == null) {
						entries = new ArrayList<>();
						playerModelEntries.put(playerId, entries);
					}
					entries.add(pme);
				}
			}

			@Override
			public void setParams(PreparedStatement stmt) throws SQLException {
			}
		});

		return playerModelEntries;
	}

	@Override
	public void insertNewRecords(Collection<PlayerModelEntry> filteredEntries) {
		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement stmt = con.prepareStatement(INSERT_QUERY)) {
			con.setAutoCommit(false);
			for (PlayerModelEntry pme : filteredEntries) {
				stmt.setInt(1, pme.getPlayerID());
				stmt.setTimestamp(2, pme.getTimestamp());
				stmt.setInt(3, pme.getSkillID());
				stmt.setInt(4, pme.getPlayerClassID());
				stmt.setFloat(5, pme.getPlayerHPpercentage());
				stmt.setFloat(6, pme.getPlayerMPpercentage());
				stmt.setFloat(7, pme.getPlayerX());
				stmt.setFloat(8, pme.getPlayerY());
				stmt.setFloat(9, pme.getPlayerZ());
				stmt.setBoolean(10, pme.isPlayerRooted());
				stmt.setBoolean(11, pme.isPlayerSilenced());
				stmt.setBoolean(12, pme.isPlayerBound());
				stmt.setBoolean(13, pme.isPlayerStunned());
				stmt.setBoolean(14, pme.isPlayerAetherhold());
				stmt.setInt(15, pme.getPlayerBuffCount());
				stmt.setInt(16, pme.getPlayerDebuffCount());
				stmt.setBoolean(17, pme.isPlayerIsShielded());
				stmt.setInt(18, pme.getTargetID());
				stmt.setInt(19, pme.getTargetClassID());
				stmt.setFloat(20, pme.getTargetHPpercentage());
				stmt.setFloat(21, pme.getTargetMPpercentage());
				stmt.setBoolean(22, pme.isTargetIsPvP());
				stmt.setBoolean(23, pme.isTargetFocusesPlayer());
				stmt.setFloat(24, pme.getDistance());
				stmt.setFloat(25, pme.getTargetX());
				stmt.setFloat(26, pme.getTargetY());
				stmt.setFloat(27, pme.getTargetZ());
				stmt.setBoolean(28, pme.isTargetRooted());
				stmt.setBoolean(29, pme.isTargetSilenced());
				stmt.setBoolean(30, pme.isTargetBound());
				stmt.setBoolean(31, pme.isTargetStunned());
				stmt.setBoolean(32, pme.isTargetAetherhold());
				stmt.setInt(33, pme.getTargetBuffCount());
				stmt.setInt(34, pme.getTargetDebuffCount());
				stmt.setBoolean(35, pme.isTargetIsShielded());
				stmt.setBoolean(36, pme.isBossPhase());
				stmt.addBatch();

				pme.setPersistentState(PersistentState.UPDATED);
			}
			stmt.executeBatch();
			con.commit();
		} catch (Exception e) {
			LoggerFactory.getLogger(MySQL5CustomInstancePlayerModelEntryDAO.class).error("Error occured while saving player model entries.", e);
		}
	}

	@Override
	public boolean supports(String databaseName, int majorVersion, int minorVersion) {
		return MySQL5DAOUtils.supports(databaseName, majorVersion, minorVersion);
	}
}
