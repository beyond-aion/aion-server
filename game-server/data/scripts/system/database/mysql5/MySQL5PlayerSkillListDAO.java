package mysql5;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.database.DatabaseFactory;
import com.aionemu.commons.utils.GenericValidator;
import com.aionemu.gameserver.dao.MySQL5DAOUtils;
import com.aionemu.gameserver.dao.PlayerSkillListDAO;
import com.aionemu.gameserver.model.gameobjects.PersistentState;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.skill.PlayerSkillEntry;
import com.aionemu.gameserver.model.skill.PlayerSkillList;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;

import javolution.util.FastTable;

/**
 * @author SoulKeeper
 * @author IceReaper, orfeo087, Avol, AEJTester
 */
public class MySQL5PlayerSkillListDAO extends PlayerSkillListDAO {

	private static final Logger log = LoggerFactory.getLogger(MySQL5PlayerSkillListDAO.class);
	public static final String INSERT_QUERY = "REPLACE INTO `player_skills` (`player_id`, `skill_id`, `skill_level`) VALUES (?, ?, ?)";
	public static final String UPDATE_QUERY = "UPDATE `player_skills` set skill_level=? where player_id=? AND skill_id=?";
	public static final String DELETE_QUERY = "DELETE FROM `player_skills` WHERE `player_id`=? AND skill_id=?";
	public static final String SELECT_QUERY = "SELECT `skill_id`, `skill_level` FROM `player_skills` WHERE `player_id`=?";

	private static final Predicate<PlayerSkillEntry> skillsToInsertPredicate = new Predicate<PlayerSkillEntry>() {

		@Override
		public boolean apply(@Nullable PlayerSkillEntry input) {
			return input != null && PersistentState.NEW == input.getPersistentState();
		}
	};

	private static final Predicate<PlayerSkillEntry> skillsToUpdatePredicate = new Predicate<PlayerSkillEntry>() {

		@Override
		public boolean apply(@Nullable PlayerSkillEntry input) {
			return input != null && PersistentState.UPDATE_REQUIRED == input.getPersistentState();
		}
	};

	private static final Predicate<PlayerSkillEntry> skillsToDeletePredicate = new Predicate<PlayerSkillEntry>() {

		@Override
		public boolean apply(@Nullable PlayerSkillEntry input) {
			return input != null && PersistentState.DELETED == input.getPersistentState();
		}
	};

	@Override
	public PlayerSkillList loadSkillList(int playerId) {
		List<PlayerSkillEntry> skills = new FastTable<>();
		try {
			try (Connection con = DatabaseFactory.getConnection(); PreparedStatement stmt = con.prepareStatement(SELECT_QUERY)) {
				stmt.setInt(1, playerId);
				try (ResultSet rset = stmt.executeQuery()) {
					while (rset.next()) {
						int id = rset.getInt("skill_id");
						int lv = rset.getInt("skill_level");
						skills.add(new PlayerSkillEntry(id, lv, 0, PersistentState.UPDATED));
					}
				}
			}
		} catch (Exception e) {
			log.error("Could not restore SkillList data for player: " + playerId + " from DB: " + e.getMessage(), e);
		}
		return new PlayerSkillList(skills);
	}

	/**
	 * Stores all player skills according to their persistence state
	 */
	@Override
	public boolean storeSkills(Player player) {
		store(player, player.getSkillList().getDeletedSkills());
		store(player, player.getSkillList().getAllSkills());

		return true;
	}

	private void store(Player player, List<PlayerSkillEntry> skills) {
		Connection con = null;
		try {
			con = DatabaseFactory.getConnection();
			con.setAutoCommit(false);

			deleteSkills(con, player, skills);
			addSkills(con, player, skills);
			updateSkills(con, player, skills);

		} catch (SQLException e) {
			log.error("Failed to open connection to database while saving SkillList for player " + player.getObjectId());
		} finally {
			DatabaseFactory.close(con);
		}

		for (PlayerSkillEntry skill : skills) {
			skill.setPersistentState(PersistentState.UPDATED);
		}
	}

	private void addSkills(Connection con, Player player, List<PlayerSkillEntry> skills) {

		Collection<PlayerSkillEntry> skillsToInsert = Collections2.filter(skills, skillsToInsertPredicate);
		if (GenericValidator.isBlankOrNull(skillsToInsert)) {
			return;
		}

		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement(INSERT_QUERY);

			for (PlayerSkillEntry skill : skillsToInsert) {
				ps.setInt(1, player.getObjectId());
				ps.setInt(2, skill.getSkillId());
				ps.setInt(3, skill.getSkillLevel());
				ps.addBatch();
			}

			ps.executeBatch();
			con.commit();
		} catch (SQLException e) {
			log.error("Can't add skills for player: " + player.getObjectId(), e);
		} finally {
			DatabaseFactory.close(ps);
		}
	}

	private void updateSkills(Connection con, Player player, List<PlayerSkillEntry> skills) {

		Collection<PlayerSkillEntry> skillsToUpdate = Collections2.filter(skills, skillsToUpdatePredicate);
		if (GenericValidator.isBlankOrNull(skillsToUpdate)) {
			return;
		}

		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement(UPDATE_QUERY);

			for (PlayerSkillEntry skill : skillsToUpdate) {
				ps.setInt(1, skill.getSkillLevel());
				ps.setInt(2, player.getObjectId());
				ps.setInt(3, skill.getSkillId());
				ps.addBatch();
			}

			ps.executeBatch();
			con.commit();
		} catch (SQLException e) {
			log.error("Can't update skills for player: " + player.getObjectId());
		} finally {
			DatabaseFactory.close(ps);
		}
	}

	private void deleteSkills(Connection con, Player player, List<PlayerSkillEntry> skills) {

		Collection<PlayerSkillEntry> skillsToDelete = Collections2.filter(skills, skillsToDeletePredicate);
		if (GenericValidator.isBlankOrNull(skillsToDelete)) {
			return;
		}

		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement(DELETE_QUERY);

			for (PlayerSkillEntry skill : skillsToDelete) {
				ps.setInt(1, player.getObjectId());
				ps.setInt(2, skill.getSkillId());
				ps.addBatch();
			}

			ps.executeBatch();
			con.commit();
		} catch (SQLException e) {
			log.error("Can't delete skills for player: " + player.getObjectId());
		} finally {
			DatabaseFactory.close(ps);
		}
	}

	@Override
	public boolean supports(String databaseName, int majorVersion, int minorVersion) {
		return MySQL5DAOUtils.supports(databaseName, majorVersion, minorVersion);
	}
}
