package mysql5;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.database.DB;
import com.aionemu.commons.database.DatabaseFactory;
import com.aionemu.commons.database.IUStH;
import com.aionemu.commons.database.ParamReadStH;
import com.aionemu.commons.utils.GenericValidator;
import com.aionemu.gameserver.configs.main.CacheConfig;
import com.aionemu.gameserver.configs.main.GSConfig;
import com.aionemu.gameserver.dao.MySQL5DAOUtils;
import com.aionemu.gameserver.dao.PlayerDAO;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.Gender;
import com.aionemu.gameserver.model.PlayerClass;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.account.PlayerAccountData;
import com.aionemu.gameserver.model.gameobjects.player.Mailbox;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.player.PlayerCommonData;
import com.aionemu.gameserver.world.World;

/**
 * @author SoulKeeper, Saelya, cura, KID
 */
public class MySQL5PlayerDAO extends PlayerDAO {

	private static final Logger log = LoggerFactory.getLogger(MySQL5PlayerDAO.class);
	private ConcurrentHashMap<Integer, PlayerCommonData> playerCommonData = new ConcurrentHashMap<>();
	private ConcurrentHashMap<String, PlayerCommonData> playerCommonDataByName = new ConcurrentHashMap<>();

	@Override
	public boolean isNameUsed(final String name) {
		PreparedStatement s = DB.prepareStatement("SELECT count(id) as cnt FROM players WHERE ? = players.name");
		try {
			s.setString(1, name);
			ResultSet rs = s.executeQuery();
			rs.next();
			return rs.getInt("cnt") > 0;
		} catch (SQLException e) {
			log.error("Can't check if name " + name + ", is used, returning possitive result", e);
			return true;
		} finally {
			DB.close(s);
		}
	}

	@Override
	public Map<Integer, String> getPlayerNames(Collection<Integer> playerObjectIds) {

		if (GenericValidator.isBlankOrNull(playerObjectIds)) {
			return Collections.emptyMap();
		}

		Map<Integer, String> result = new HashMap<>();

		String sql = "SELECT id, `name` FROM players WHERE id IN(%s)";
		sql = String.format(sql, StringUtils.join(playerObjectIds, ", "));
		PreparedStatement s = DB.prepareStatement(sql);
		try {
			ResultSet rs = s.executeQuery();
			while (rs.next()) {
				int id = rs.getInt("id");
				String name = rs.getString("name");
				result.put(id, name);
			}
		} catch (SQLException e) {
			throw new RuntimeException("Failed to load player names", e);
		} finally {
			DB.close(s);
		}

		return result;
	}

	@Override
	public void changePlayerAccountId(final Player player, final int accountId) {
		try {
			try (Connection con = DatabaseFactory.getConnection();
				PreparedStatement stmt = con.prepareStatement("UPDATE players SET account_id=? WHERE id=?")) {
				stmt.setInt(1, accountId);
				stmt.setInt(2, player.getObjectId());
				stmt.execute();
			}
		} catch (Exception e) {
			log.error("Error saving player: " + player.getObjectId() + " " + player.getName(), e);
		}
	}

	@Override
	public void storePlayer(final Player player) {
		try {
			try (Connection con = DatabaseFactory.getConnection();
				PreparedStatement stmt = con.prepareStatement(
					"UPDATE players SET name=?, exp=?, recoverexp=?, x=?, y=?, z=?, heading=?, world_id=?, gender=?, race=?, player_class=?, quest_expands=?, npc_expands=?, item_expands=?, wh_npc_expands=?, wh_bonus_expands=?, note=?, title_id=?, bonus_title_id=?, dp=?, soul_sickness=?, mailbox_letters=?, reposte_energy=?, mentor_flag_time=?, world_owner=? WHERE id=?")) {
				log.debug("[DAO: MySQL5PlayerDAO] storing player " + player.getObjectId() + " " + player.getName());
				PlayerCommonData pcd = player.getCommonData();
				stmt.setString(1, player.getName());
				stmt.setLong(2, pcd.getExp());
				stmt.setLong(3, pcd.getExpRecoverable());
				stmt.setFloat(4, player.getX());
				stmt.setFloat(5, player.getY());
				stmt.setFloat(6, player.getZ());
				stmt.setInt(7, player.getHeading());
				stmt.setInt(8, player.getWorldId());
				stmt.setString(9, player.getGender().toString());
				stmt.setString(10, player.getRace().toString());
				stmt.setString(11, pcd.getPlayerClass().toString());
				stmt.setInt(12, player.getQuestExpands());
				stmt.setInt(13, player.getNpcExpands());
				stmt.setInt(14, player.getItemExpands());
				stmt.setInt(15, player.getWhNpcExpands());
				stmt.setInt(16, player.getWhBonusExpands());
				stmt.setString(17, pcd.getNote());
				stmt.setInt(18, pcd.getTitleId());
				stmt.setInt(19, pcd.getBonusTitleId());
				stmt.setInt(20, pcd.getDp());
				stmt.setInt(21, pcd.getDeathCount());
				Mailbox mailBox = player.getMailbox();
				int mails = mailBox != null ? mailBox.size() : pcd.getMailboxLetters();
				stmt.setInt(22, mails);
				stmt.setLong(23, pcd.getCurrentReposeEnergy());
				stmt.setInt(24, pcd.getMentorFlagTime());
				stmt.setInt(25, player.getPosition().getMapRegion() == null ? 0 : player.getPosition().getWorldMapInstance().getOwnerId());
				stmt.setInt(26, player.getObjectId());
				stmt.execute();
			}
		} catch (Exception e) {
			log.error("Error saving player: " + player.getObjectId() + " " + player.getName(), e);
		}
		if (CacheConfig.CACHE_COMMONDATA) {
			PlayerCommonData cached = playerCommonData.get(player.getObjectId());
			if (cached != null) {
				playerCommonData.put(player.getCommonData().getPlayerObjId(), player.getCommonData());
				playerCommonDataByName.put(player.getName().toLowerCase(), player.getCommonData());
			}
		}
	}

	@Override
	public boolean saveNewPlayer(final Player player, final int accountId, final String accountName) {
		try {
			try (Connection con = DatabaseFactory.getConnection();
				PreparedStatement stmt = con.prepareStatement(
					"INSERT INTO players(id, `name`, account_id, account_name, x, y, z, heading, world_id, gender, race, player_class , quest_expands, npc_expands, item_expands, wh_npc_expands, wh_bonus_expands, online) "
						+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 0)")) {
				log.debug("[DAO: MySQL5PlayerDAO] saving new player: " + player.getObjectId() + " " + player.getName());

				stmt.setInt(1, player.getObjectId());
				stmt.setString(2, player.getName());
				stmt.setInt(3, accountId);
				stmt.setString(4, accountName);
				stmt.setFloat(5, player.getPosition().getX());
				stmt.setFloat(6, player.getPosition().getY());
				stmt.setFloat(7, player.getPosition().getZ());
				stmt.setInt(8, player.getPosition().getHeading());
				stmt.setInt(9, player.getPosition().getMapId());
				stmt.setString(10, player.getGender().toString());
				stmt.setString(11, player.getRace().toString());
				stmt.setString(12, player.getPlayerClass().toString());
				stmt.setInt(13, player.getQuestExpands());
				stmt.setInt(14, player.getNpcExpands());
				stmt.setInt(15, player.getItemExpands());
				stmt.setInt(16, player.getWhNpcExpands());
				stmt.setInt(17, player.getWhBonusExpands());
				stmt.execute();
			}
		} catch (Exception e) {
			log.error("Error saving new player: " + player.getObjectId() + " " + player.getName(), e);
			return false;
		}
		if (CacheConfig.CACHE_COMMONDATA) {
			playerCommonData.put(player.getObjectId(), player.getCommonData());
			playerCommonDataByName.put(player.getName().toLowerCase(), player.getCommonData());
		}
		return true;
	}

	@Override
	public PlayerCommonData loadPlayerCommonDataByName(final String name) {
		Player player = World.getInstance().findPlayer(name);
		if (player != null) {
			return player.getCommonData();
		}
		PlayerCommonData pcd = playerCommonDataByName.get(name.toLowerCase());
		if (pcd != null) {
			return pcd;
		}
		int playerObjId = 0;

		try {
			try (Connection con = DatabaseFactory.getConnection(); PreparedStatement stmt = con.prepareStatement("SELECT id FROM players WHERE name = ?")) {
				stmt.setString(1, name);
				try (ResultSet rset = stmt.executeQuery()) {
					if (rset.next()) {
						playerObjId = rset.getInt("id");
					}
				}
			}
		} catch (Exception e) {
			log.error("Could not restore playerId data for player name: " + name + " from DB: " + e.getMessage(), e);
		}

		if (playerObjId == 0) {
			return null;
		}
		return loadPlayerCommonData(playerObjId);
	}

	@Override
	public PlayerCommonData loadPlayerCommonData(final int playerObjId) {

		PlayerCommonData cached = playerCommonData.get(playerObjId);
		if (cached != null) {
			log.debug("[DAO: MySQL5PlayerDAO] PlayerCommonData for id: " + playerObjId + " obtained from cache");
			return cached;
		}
		final PlayerCommonData cd = new PlayerCommonData(playerObjId);
		boolean success = false;

		try {
			try (Connection con = DatabaseFactory.getConnection(); PreparedStatement stmt = con.prepareStatement("SELECT * FROM players WHERE id = ?")) {
				stmt.setInt(1, playerObjId);
				try (ResultSet resultSet = stmt.executeQuery()) {
					log.debug("[DAO: MySQL5PlayerDAO] loading from db " + playerObjId);

					if (resultSet.next()) {
						success = true;
						cd.setName(resultSet.getString("name"));
						cd.setPlayerClass(PlayerClass.valueOf(resultSet.getString("player_class")));
						cd.setExp(resultSet.getLong("exp")); // set class before exp for daeva determination
						cd.setRecoverableExp(resultSet.getLong("recoverexp"));
						cd.setRace(Race.valueOf(resultSet.getString("race")));
						cd.setGender(Gender.valueOf(resultSet.getString("gender")));
						cd.setLastOnline(resultSet.getTimestamp("last_online"));
						cd.setNote(resultSet.getString("note"));
						cd.setQuestExpands(resultSet.getInt("quest_expands"));
						cd.setNpcExpands(resultSet.getInt("npc_expands"));
						cd.setItemExpands(resultSet.getInt("item_expands"));
						cd.setTitleId(resultSet.getInt("title_id"));
						cd.setBonusTitleId(resultSet.getInt("bonus_title_id"));
						cd.setWhNpcExpands(resultSet.getInt("wh_npc_expands"));
						cd.setWhBonusExpands(resultSet.getInt("wh_bonus_expands"));
						cd.setOnline(resultSet.getBoolean("online"));
						cd.setMailboxLetters(resultSet.getInt("mailbox_letters"));
						cd.setDp(resultSet.getInt("dp"));
						cd.setDeathCount(resultSet.getInt("soul_sickness"));
						cd.setCurrentReposeEnergy(resultSet.getLong("reposte_energy"));
						float x = resultSet.getFloat("x");
						float y = resultSet.getFloat("y");
						float z = resultSet.getFloat("z");
						byte heading = resultSet.getByte("heading");
						int worldId = resultSet.getInt("world_id");
						cd.setPosition(World.getInstance().createPosition(worldId, x, y, z, heading, 0));
						cd.setWorldOwnerId(resultSet.getInt("world_owner"));
						cd.setMentorFlagTime(resultSet.getInt("mentor_flag_time"));
						cd.setLastTransferTime(resultSet.getLong("last_transfer_time"));
					} else {
						log.info("Missing PlayerCommonData from db " + playerObjId);
					}
				}
			}
		} catch (Exception e) {
			log.error("Could not restore PlayerCommonData data for player: " + playerObjId + " from DB: " + e.getMessage(), e);
		}

		if (success) {
			if (CacheConfig.CACHE_COMMONDATA) {
				playerCommonData.put(playerObjId, cd);
				playerCommonDataByName.put(cd.getName().toLowerCase(), cd);
			}
			return cd;
		}
		return null;
	}

	@Override
	public void deletePlayer(int playerId) {
		PreparedStatement statement = DB.prepareStatement("DELETE FROM players WHERE id = ?");
		try {
			statement.setInt(1, playerId);
		} catch (SQLException e) {
			log.error("Some crap, can't set int parameter to PreparedStatement", e);
		}
		if (CacheConfig.CACHE_COMMONDATA) {
			PlayerCommonData pcd = playerCommonData.remove(playerId);
			if (pcd != null) {
				playerCommonDataByName.remove(pcd.getName().toLowerCase());
			}
		}
		DB.executeUpdateAndClose(statement);
	}

	@Override
	public List<Integer> getPlayerOidsOnAccount(final int accountId) {
		final List<Integer> result = new ArrayList<>();
		boolean success = DB.select("SELECT id FROM players WHERE account_id = ?", new ParamReadStH() {

			@Override
			public void handleRead(ResultSet resultSet) throws SQLException {
				while (resultSet.next()) {
					result.add(resultSet.getInt("id"));
				}
			}

			@Override
			public void setParams(PreparedStatement preparedStatement) throws SQLException {
				preparedStatement.setInt(1, accountId);
			}
		});

		return success ? result : null;
	}

	@Override
	public List<Integer> getPlayerOidsOnAccount(final int accountId, final long exp) {
		final List<Integer> result = new ArrayList<>();
		boolean success = DB.select("SELECT id FROM players WHERE account_id = ? AND exp <= ?", new ParamReadStH() {

			@Override
			public void handleRead(ResultSet resultSet) throws SQLException {
				while (resultSet.next()) {
					result.add(resultSet.getInt("id"));
				}
			}

			@Override
			public void setParams(PreparedStatement preparedStatement) throws SQLException {
				preparedStatement.setInt(1, accountId);
				preparedStatement.setLong(2, exp);
			}
		});

		return success ? result : null;
	}

	@Override
	public void setCreationDeletionTime(final PlayerAccountData acData) {
		DB.select("SELECT creation_date, deletion_date FROM players WHERE id = ?", new ParamReadStH() {

			@Override
			public void setParams(PreparedStatement stmt) throws SQLException {
				stmt.setInt(1, acData.getPlayerCommonData().getPlayerObjId());
			}

			@Override
			public void handleRead(ResultSet rset) throws SQLException {
				rset.next();

				acData.setDeletionDate(rset.getTimestamp("deletion_date"));
				acData.setCreationDate(rset.getTimestamp("creation_date"));
			}
		});
	}

	@Override
	public void updateDeletionTime(final int objectId, final Timestamp deletionDate) {
		DB.insertUpdate("UPDATE players set deletion_date = ? where id = ?", new IUStH() {

			@Override
			public void handleInsertUpdate(PreparedStatement preparedStatement) throws SQLException {
				preparedStatement.setTimestamp(1, deletionDate);
				preparedStatement.setInt(2, objectId);
				preparedStatement.execute();
			}
		});
	}

	@Override
	public void storeCreationTime(final int objectId, final Timestamp creationDate) {
		DB.insertUpdate("UPDATE players set creation_date = ? where id = ?", new IUStH() {

			@Override
			public void handleInsertUpdate(PreparedStatement preparedStatement) throws SQLException {
				preparedStatement.setTimestamp(1, creationDate);
				preparedStatement.setInt(2, objectId);
				preparedStatement.execute();
			}
		});
	}

	@Override
	public void storeLastOnlineTime(final int objectId, final Timestamp lastOnline) {
		DB.insertUpdate("UPDATE players set last_online = ? where id = ?", new IUStH() {

			@Override
			public void handleInsertUpdate(PreparedStatement preparedStatement) throws SQLException {
				preparedStatement.setTimestamp(1, lastOnline);
				preparedStatement.setInt(2, objectId);
				preparedStatement.execute();
			}
		});
	}

	@Override
	public int[] getUsedIDs() {
		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement stmt = con.prepareStatement("SELECT id FROM players", ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY)) {
			ResultSet rs = stmt.executeQuery();
			rs.last();
			int count = rs.getRow();
			rs.beforeFirst();
			int[] ids = new int[count];
			for (int i = 0; rs.next(); i++)
				ids[i] = rs.getInt("id");
			return ids;
		} catch (SQLException e) {
			log.error("Can't get list of IDs from players table", e);
			return null;
		}
	}

	@Override
	public boolean isOnline(int playerId) {
		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement stmt = con.prepareStatement("SELECT online FROM players WHERE id=?")) {
			stmt.setInt(1, playerId);
			ResultSet rs = stmt.executeQuery();
			if (rs.next())
				return rs.getBoolean("online");
		} catch (SQLException e) {
			log.error("Can't get online state of player " + playerId, e);
		}
		return false;
	}

	@Override
	public void onlinePlayer(final Player player, final boolean online) {
		DB.insertUpdate("UPDATE players SET online=? WHERE id=?", new IUStH() {

			@Override
			public void handleInsertUpdate(PreparedStatement stmt) throws SQLException {
				log.debug("[DAO: MySQL5PlayerDAO] online status " + player.getObjectId() + " " + player.getName());

				stmt.setBoolean(1, online);
				stmt.setInt(2, player.getObjectId());
				stmt.execute();
			}
		});
	}

	@Override
	public void setAllPlayersOffline() {
		DB.insertUpdate("UPDATE players SET online=?", new IUStH() {

			@Override
			public void handleInsertUpdate(PreparedStatement stmt) throws SQLException {
				stmt.setBoolean(1, false);
				stmt.execute();
			}
		});
	}

	@Override
	public String getPlayerNameByObjId(final int playerObjId) {
		final String[] result = new String[1];
		DB.select("SELECT name FROM players WHERE id = ?", new ParamReadStH() {

			@Override
			public void handleRead(ResultSet arg0) throws SQLException {
				// TODO: Auto-generated method stub
				arg0.next();
				result[0] = arg0.getString("name");
			}

			@Override
			public void setParams(PreparedStatement arg0) throws SQLException {
				// TODO: Auto-generated method stub
				arg0.setInt(1, playerObjId);
			}
		});
		return result[0];
	}

	@Override
	public int getPlayerIdByName(final String playerName) {
		final int[] result = new int[1];
		DB.select("SELECT id FROM players WHERE name = ?", new ParamReadStH() {

			@Override
			public void handleRead(ResultSet arg0) throws SQLException {
				// TODO: Auto-generated method stub
				arg0.next();
				result[0] = arg0.getInt("id");
			}

			@Override
			public void setParams(PreparedStatement arg0) throws SQLException {
				// TODO: Auto-generated method stub
				arg0.setString(1, playerName);
			}
		});
		return result[0];
	}

	@Override
	public int getAccountIdByName(final String name) {
		int accountId = 0;
		try {
			try (Connection con = DatabaseFactory.getConnection();
				PreparedStatement stmt = con.prepareStatement("SELECT `account_id` FROM `players` WHERE `name` = ?")) {
				stmt.setString(1, name);
				try (ResultSet rs = stmt.executeQuery()) {
					rs.next();
					accountId = rs.getInt("account_id");
				}
			}
		} catch (Exception e) {
			return 0;
		}
		return accountId;
	}

	@Override
	public int getAccountId(final int playerId) {
		int accountId = 0;
		try {
			try (Connection con = DatabaseFactory.getConnection();
				PreparedStatement stmt = con.prepareStatement("SELECT `account_id` FROM `players` WHERE `id` = ?")) {
				stmt.setInt(1, playerId);
				try (ResultSet rs = stmt.executeQuery()) {
					rs.next();
					accountId = rs.getInt("account_id");
				}
			}
		} catch (Exception e) {
			return 0;
		}
		return accountId;
	}

	/**
	 * @author xTz
	 */
	@Override
	public void storePlayerName(final PlayerCommonData recipientCommonData) {
		try {
			try (Connection con = DatabaseFactory.getConnection(); PreparedStatement stmt = con.prepareStatement("UPDATE players SET name=? WHERE id=?")) {
				log.debug("[DAO: MySQL5PlayerDAO] storing playerName " + recipientCommonData.getPlayerObjId() + " " + recipientCommonData.getName());
				stmt.setString(1, recipientCommonData.getName());
				stmt.setInt(2, recipientCommonData.getPlayerObjId());
				stmt.execute();
			}
		} catch (Exception e) {
			log.error("Error saving playerName: " + recipientCommonData.getPlayerObjId() + " " + recipientCommonData.getName(), e);
		}
	}

	@Override
	public int getCharacterCountOnAccount(final int accountId) {
		int cnt = 0;
		try {
			try (Connection con = DatabaseFactory.getConnection();
				PreparedStatement stmt = con.prepareStatement(
					"SELECT COUNT(*) AS cnt FROM `players` WHERE `account_id` = ? AND (players.deletion_date IS NULL || players.deletion_date > CURRENT_TIMESTAMP)")) {
				stmt.setInt(1, accountId);
				try (ResultSet rs = stmt.executeQuery()) {
					rs.next();
					cnt = rs.getInt("cnt");
				}
			}
		} catch (Exception e) {
			return 0;
		}
		return cnt;
	}

	@Override
	public int getCharacterCountForRace(Race race) {
		int count = 0;
		try {
			try (Connection con = DatabaseFactory.getConnection();
				PreparedStatement stmt = con
					.prepareStatement("SELECT COUNT(DISTINCT(`account_id`)) AS `count` FROM `players` WHERE `race` = ? AND `exp` >= ?")) {
				stmt.setString(1, race.name());
				stmt.setLong(2, DataManager.PLAYER_EXPERIENCE_TABLE.getStartExpForLevel(GSConfig.RATIO_MIN_REQUIRED_LEVEL));
				try (ResultSet rs = stmt.executeQuery()) {
					rs.next();
					count = rs.getInt("count");
				}
			}
		} catch (Exception e) {
			return 0;
		}
		return count;
	}

	@Override
	public Set<Integer> getInactiveAccounts(final int daysOfInactivity) {
		String SELECT_QUERY = "SELECT account_id FROM players WHERE UNIX_TIMESTAMP(CURDATE())-UNIX_TIMESTAMP(last_online) > ? * 24 * 60 * 60";

		final Map<Integer, Integer> inactiveAccounts = new HashMap<>();

		DB.select(SELECT_QUERY, new ParamReadStH() {

			@Override
			public void setParams(PreparedStatement stmt) throws SQLException {
				stmt.setInt(1, daysOfInactivity);
			}

			@Override
			public void handleRead(ResultSet rset) throws SQLException {
				while (rset.next()) {
					int accountId = rset.getInt("account_id");
					// number of inactive chars on account
					Integer numberOfChars = 0;

					if ((numberOfChars = inactiveAccounts.get(accountId)) != null) {
						inactiveAccounts.put(accountId, numberOfChars + 1);
					} else {
						inactiveAccounts.put(accountId, 1);
					}
				}
			}
		});

		// filter accounts with active chars on them
		for (Iterator<Entry<Integer, Integer>> i = inactiveAccounts.entrySet().iterator(); i.hasNext();) {
			Entry<Integer, Integer> entry = i.next();

			// atleast one active char on account
			if (entry.getValue() < this.getCharacterCountOnAccount(entry.getKey())) {
				i.remove();
			}
		}

		return inactiveAccounts.keySet();
	}

	@Override
	public void setPlayerLastTransferTime(final int playerId, final long time) {
		DB.insertUpdate("UPDATE players SET last_transfer_time=? WHERE id=?", new IUStH() {

			@Override
			public void handleInsertUpdate(PreparedStatement stmt) throws SQLException {
				stmt.setLong(1, time);
				stmt.setInt(2, playerId);
				stmt.execute();
			}
		});
	}

	@Override
	public int getOldCharacterLevel(int playerObjectId) {
		int oldLevel = 0;
		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement stmt = con.prepareStatement("SELECT old_level FROM players WHERE id=?")) {
			stmt.setInt(1, playerObjectId);
			try (ResultSet rs = stmt.executeQuery()) {
				rs.next();
				oldLevel = rs.getInt("old_level");
			}
		} catch (Exception e) {
			log.error("Error reading old_level for player: " + playerObjectId, e);
		}
		return oldLevel;
	}

	@Override
	public void storeOldCharacterLevel(int playerObjectId, int level) {
		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement stmt = con.prepareStatement("UPDATE players SET old_level=? WHERE id=?")) {
			stmt.setInt(1, level);
			stmt.setInt(2, playerObjectId);
			stmt.execute();
		} catch (Exception e) {
			log.error("Error storing old_level: " + level + " for player: " + playerObjectId, e);
		}
	}

	@Override
	public boolean supports(String s, int i, int i1) {
		return MySQL5DAOUtils.supports(s, i, i1);
	}
}
