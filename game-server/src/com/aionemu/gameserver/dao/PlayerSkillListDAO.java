package com.aionemu.gameserver.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.database.DatabaseFactory;
import com.aionemu.gameserver.model.gameobjects.Persistable;
import com.aionemu.gameserver.model.gameobjects.Persistable.PersistentState;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.skill.PlayerSkillEntry;
import com.aionemu.gameserver.model.skill.PlayerSkillList;

/**
 * Created on: 15.07.2009 19:33:07 Edited On: 13.09.2009 19:48:00
 * 
 * @author SoulKeeper, IceReaper, orfeo087, Avol, AEJTester
 */
public class PlayerSkillListDAO {

	private static final Logger log = LoggerFactory.getLogger(PlayerSkillListDAO.class);

	public static final String INSERT_QUERY = "REPLACE INTO `player_skills` (`player_id`, `skill_id`, `skill_level`) VALUES (?, ?, ?)";
	public static final String UPDATE_QUERY = "UPDATE `player_skills` set skill_level=? where player_id=? AND skill_id=?";
	public static final String DELETE_QUERY = "DELETE FROM `player_skills` WHERE `player_id`=? AND skill_id=?";
	public static final String SELECT_QUERY = "SELECT `skill_id`, `skill_level` FROM `player_skills` WHERE `player_id`=?";

	public static PlayerSkillList loadSkillList(int playerId) {
		List<PlayerSkillEntry> skills = new ArrayList<>();
		try (Connection con = DatabaseFactory.getConnection();
				 PreparedStatement stmt = con.prepareStatement(SELECT_QUERY)) {
			stmt.setInt(1, playerId);
			try (ResultSet rset = stmt.executeQuery()) {
				while (rset.next()) {
					int id = rset.getInt("skill_id");
					int lv = rset.getInt("skill_level");
					skills.add(new PlayerSkillEntry(id, lv, 0, PersistentState.UPDATED));
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
	public static boolean storeSkills(Player player) {
		store(player, player.getSkillList().getDeletedSkills());
		store(player, player.getSkillList().getAllSkills());

		return true;
	}

	private static void store(Player player, List<PlayerSkillEntry> skills) {
		try (Connection con = DatabaseFactory.getConnection()) {
			con.setAutoCommit(false);

			deleteSkills(con, player, skills);
			addSkills(con, player, skills);
			updateSkills(con, player, skills);

		} catch (SQLException e) {
			log.error("Failed to open connection to database while saving SkillList for player " + player.getObjectId());
		}

		for (PlayerSkillEntry skill : skills) {
			skill.setPersistentState(PersistentState.UPDATED);
		}
	}

	private static void addSkills(Connection con, Player player, List<PlayerSkillEntry> skills) {
		skills = skills.stream().filter(Persistable.NEW).collect(Collectors.toList());
		if (skills.isEmpty())
			return;

		try (PreparedStatement ps = con.prepareStatement(INSERT_QUERY)) {
			for (PlayerSkillEntry skill : skills) {
				ps.setInt(1, player.getObjectId());
				ps.setInt(2, skill.getSkillId());
				ps.setInt(3, skill.getSkillLevel());
				ps.addBatch();
			}

			ps.executeBatch();
			con.commit();
		} catch (SQLException e) {
			log.error("Can't add skills for player: " + player.getObjectId(), e);
		}
	}

	private static void updateSkills(Connection con, Player player, List<PlayerSkillEntry> skills) {
		skills = skills.stream().filter(Persistable.CHANGED).collect(Collectors.toList());
		if (skills.isEmpty())
			return;

		try (PreparedStatement ps = con.prepareStatement(UPDATE_QUERY)) {
			for (PlayerSkillEntry skill : skills) {
				ps.setInt(1, skill.getSkillLevel());
				ps.setInt(2, player.getObjectId());
				ps.setInt(3, skill.getSkillId());
				ps.addBatch();
			}

			ps.executeBatch();
			con.commit();
		} catch (SQLException e) {
			log.error("Can't update skills for player: " + player.getObjectId());
		}
	}

	private static void deleteSkills(Connection con, Player player, List<PlayerSkillEntry> skills) {
		skills = skills.stream().filter(Persistable.DELETED).collect(Collectors.toList());
		if (skills.isEmpty())
			return;

		try (PreparedStatement ps = con.prepareStatement(DELETE_QUERY)) {
			for (PlayerSkillEntry skill : skills) {
				ps.setInt(1, player.getObjectId());
				ps.setInt(2, skill.getSkillId());
				ps.addBatch();
			}

			ps.executeBatch();
			con.commit();
		} catch (SQLException e) {
			log.error("Can't delete skills for player: " + player.getObjectId());
		}
	}

}
