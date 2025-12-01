package com.aionemu.gameserver.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.database.DB;
import com.aionemu.commons.database.DatabaseFactory;
import com.aionemu.commons.database.IUStH;
import com.aionemu.gameserver.model.team.legion.Legion;
import com.aionemu.gameserver.model.team.legion.LegionMember;
import com.aionemu.gameserver.model.team.legion.LegionRank;
import com.aionemu.gameserver.services.LegionService;

/**
 * Class that is responsible for storing/loading legion data
 * 
 * @author Simple
 */
public class LegionMemberDAO {

	/** Logger */
	private static final Logger log = LoggerFactory.getLogger(LegionMemberDAO.class);
	/** LegionMember Queries */
	private static final String INSERT_LEGIONMEMBER_QUERY = "INSERT INTO legion_members(`legion_id`, `player_id`, `rank`) VALUES (?, ?, ?)";
	private static final String UPDATE_LEGIONMEMBER_QUERY = "UPDATE legion_members SET nickname=?, `rank`=?, selfintro=?, challenge_score=? WHERE player_id=?";
	private static final String UPDATE_RANK_QUERY = "UPDATE legion_members SET `rank`=? WHERE player_id=?";
	private static final String SELECT_LEGIONMEMBER_QUERY = "SELECT * FROM legion_members WHERE player_id = ?";
	private static final String DELETE_LEGIONMEMBER_QUERY = "DELETE FROM legion_members WHERE player_id = ?";
	private static final String SELECT_LEGIONMEMBERS_QUERY = "SELECT player_id FROM legion_members WHERE legion_id = ?";

	public static boolean isIdUsed(int playerObjId) {
		PreparedStatement s = DB.prepareStatement("SELECT count(player_id) as cnt FROM legion_members WHERE ? = legion_members.player_id");
		try {
			s.setInt(1, playerObjId);
			ResultSet rs = s.executeQuery();
			rs.next();
			return rs.getInt("cnt") > 0;
		} catch (SQLException e) {
			log.error("Can't check if name " + playerObjId + ", is used, returning possitive result", e);
			return true;
		} finally {
			DB.close(s);
		}
	}

	public static boolean saveNewLegionMember(LegionMember legionMember) {
		boolean success = DB.insertUpdate(INSERT_LEGIONMEMBER_QUERY, new IUStH() {

			@Override
			public void handleInsertUpdate(PreparedStatement preparedStatement) throws SQLException {
				preparedStatement.setInt(1, legionMember.getLegion().getLegionId());
				preparedStatement.setInt(2, legionMember.getObjectId());
				preparedStatement.setString(3, legionMember.getRank().toString());
				preparedStatement.execute();
			}
		});
		return success;
	}

	public static void storeLegionMember(LegionMember legionMember) {
		DB.insertUpdate(UPDATE_LEGIONMEMBER_QUERY, new IUStH() {

			@Override
			public void handleInsertUpdate(PreparedStatement stmt) throws SQLException {
				stmt.setString(1, legionMember.getNickname());
				stmt.setString(2, legionMember.getRank().toString());
				stmt.setString(3, legionMember.getSelfIntro());
				stmt.setInt(4, legionMember.getChallengeScore());
				stmt.setInt(5, legionMember.getObjectId());
				stmt.execute();
			}
		});
	}

	public static LegionMember loadLegionMember(int playerObjId) {
		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement stmt = con.prepareStatement(SELECT_LEGIONMEMBER_QUERY)) {
			stmt.setInt(1, playerObjId);
			ResultSet resultSet = stmt.executeQuery();
			if (!resultSet.next())
				return null;
			int legionId = resultSet.getInt("legion_id");
			Legion legion = LegionService.getInstance().getLegion(legionId);
			if (legion == null) // disbanded by calling getLegion
				return null;
			LegionMember legionMember = new LegionMember(playerObjId, legion);
			legionMember.setRank(LegionRank.valueOf(resultSet.getString("rank")));
			legionMember.setNickname(resultSet.getString("nickname"));
			legionMember.setSelfIntro(resultSet.getString("selfintro"));
			legionMember.setChallengeScore(resultSet.getInt("challenge_score"));
			return legionMember;
		} catch (SQLException e) {
			log.error("Could not load legion member " + playerObjId, e);
			return null;
		}
	}

	public static List<Integer> loadLegionMembers(int legionId) {
		List<Integer> legionMembers = new ArrayList<>();
		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement stmt = con.prepareStatement(SELECT_LEGIONMEMBERS_QUERY)) {
			stmt.setInt(1, legionId);
			ResultSet rs = stmt.executeQuery();
			while (rs.next())
				legionMembers.add(rs.getInt("player_id"));
		} catch (SQLException e) {
			throw new RuntimeException("Could not load members of legion " + legionId, e);
		}
		return legionMembers;
	}

	public static void deleteLegionMember(int playerObjId) {
		PreparedStatement statement = DB.prepareStatement(DELETE_LEGIONMEMBER_QUERY);
		try {
			statement.setInt(1, playerObjId);
		} catch (SQLException e) {
			log.error("Some crap, can't set int parameter to PreparedStatement", e);
		}
		DB.executeUpdateAndClose(statement);
	}

	public static boolean setRank(int playerId, LegionRank legionRank) {
		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement stmt = con.prepareStatement(UPDATE_RANK_QUERY)) {
			stmt.setString(1, legionRank.toString());
			stmt.setInt(2, playerId);
			return stmt.executeUpdate() > 0;
		} catch (SQLException e) {
			log.error("Could not set rank of player {} to {}", playerId, legionRank, e);
			return false;
		}
	}
}
