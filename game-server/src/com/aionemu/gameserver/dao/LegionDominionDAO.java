package com.aionemu.gameserver.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.database.DB;
import com.aionemu.commons.database.DatabaseFactory;
import com.aionemu.commons.database.IUStH;
import com.aionemu.commons.database.ParamReadStH;
import com.aionemu.gameserver.model.legionDominion.LegionDominionLocation;
import com.aionemu.gameserver.model.legionDominion.LegionDominionParticipantInfo;

/**
 * @author Yeats
 */
public class LegionDominionDAO {

	private static final Logger log = LoggerFactory.getLogger(LegionDominionDAO.class);

	private static final String UPDATE_LOC = "UPDATE legion_dominion_locations SET legion_id=?, occupied_date=? WHERE id=?";
	private static final String LOAD1 = "SELECT * FROM `legion_dominion_locations`";
	private static final String LOAD2 = "SELECT * FROM `legion_dominion_participants` WHERE `legion_dominion_id`=? " ;
	private static final String INSERT_NEW_LOCATION = "INSERT INTO legion_dominion_locations(`id`,`legion_id`) VALUES(?,?)";
	private static final String INSERT_NEW = "INSERT INTO legion_dominion_participants(`legion_dominion_id`, `legion_id`) VALUES (?, ?)";
	private static final String UPDATE_PARTICIPANT = "UPDATE legion_dominion_participants SET points=?, survived_time=?, participated_date=? WHERE legion_id=?";
	private static final String DELETE_INFO = "DELETE FROM legion_dominion_participants WHERE legion_id=?";

	public static boolean loadOrCreateLegionDominionLocations(Map<Integer, LegionDominionLocation> locations) {
		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement ps = con.prepareStatement(LOAD1); ResultSet rs = ps.executeQuery()) {
			List<Integer> nonExistingLocations = new ArrayList<>(locations.keySet());
			if (rs == null) {
				log.error("Error loading Legion Dominion location from Database: empty resultset");
				return false;
			}
			while (rs.next()) {
				LegionDominionLocation loc = locations.get(rs.getInt("id"));
				loc.setLegionId(rs.getInt("legion_id"));
				loc.setOccupiedDate(rs.getTimestamp("occupied_date"));
				nonExistingLocations.remove((Integer) loc.getLocationId());
			}

			for (int locationId : nonExistingLocations) {
				try (PreparedStatement insertPs = con.prepareStatement(INSERT_NEW_LOCATION)) {
					insertPs.setInt(1, locationId);
					insertPs.setInt(2, 0);
					insertPs.execute();
				}
			}
			return true;
		} catch (SQLException e) {
			log.error("Error loading Legion Dominion location from Database", e);
			return false;
		}
	}

	public static void updateLegionDominionLocation(LegionDominionLocation loc) {
		DB.insertUpdate(UPDATE_LOC, new IUStH() {

			@Override
			public void handleInsertUpdate(PreparedStatement stmt) throws SQLException {
				stmt.setInt(1, loc.getLegionId());
				stmt.setTimestamp(2, loc.getOccupiedDate());
				stmt.setInt(3, loc.getLocationId());
				stmt.execute();
			}

		});
	}

	public static Map<Integer, LegionDominionParticipantInfo> loadParticipants(LegionDominionLocation loc) {

		Map<Integer, LegionDominionParticipantInfo> info = new TreeMap<>();
		DB.select(LOAD2, new ParamReadStH() {

			@Override
			public void handleRead(ResultSet rset) throws SQLException {
				while (rset.next()) {
					LegionDominionParticipantInfo info2 = new LegionDominionParticipantInfo();
					int legionId = rset.getInt("legion_id");
					info2.setLegionId(legionId);
					info2.setPoints(rset.getInt("points"));
					info2.setTime(rset.getInt("survived_time"));
					info2.setDate(rset.getTimestamp("participated_date"));
					if (!info.containsKey(legionId)) {
						info.put(legionId, info2);
					}
				}

			}

			@Override
			public void setParams(PreparedStatement stmt) throws SQLException {
				stmt.setInt(1, loc.getLocationId());
			}

		});
		return info;
	}

	public static void storeNewInfo(int id, LegionDominionParticipantInfo info) {
		DB.insertUpdate(INSERT_NEW, new IUStH() {

			@Override
			public void handleInsertUpdate(PreparedStatement stmt) throws SQLException {
				stmt.setInt(1, id);
				stmt.setInt(2, info.getLegionId());
				stmt.execute();
			}

		});
	}

	public static void updateInfo(LegionDominionParticipantInfo info) {
		DB.insertUpdate(UPDATE_PARTICIPANT, new IUStH() {

			@Override
			public void handleInsertUpdate(PreparedStatement stmt) throws SQLException {
				stmt.setInt(1, info.getPoints());
				stmt.setInt(2, info.getTime());
				stmt.setTimestamp(3, info.getDateAsTimeStamp());
				stmt.setInt(4, info.getLegionId());
				stmt.execute();
			}

		});
	}

	public static void delete(LegionDominionParticipantInfo info) {
		PreparedStatement statement = DB.prepareStatement(DELETE_INFO);
		try {
			statement.setInt(1, info.getLegionId());
		} catch (SQLException e) {
			log.error("Deleting ParticipantInfo", e);
		}
		DB.executeUpdateAndClose(statement);
	}

}
