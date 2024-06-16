package com.aionemu.gameserver.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.aionemu.commons.database.DB;
import com.aionemu.commons.database.IUStH;
import com.aionemu.commons.database.ParamReadStH;
import com.aionemu.gameserver.model.account.CharacterBanInfo;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.services.PunishmentService.PunishmentType;

/**
 * @author lord_rex, Cura, nrg
 */
public class PlayerPunishmentsDAO {

	public static final String SELECT_DURATIONS_QUERY = "SELECT `punishment_type`, `duration` FROM `player_punishments` WHERE `player_id`=?";
	public static final String SELECT_QUERY = "SELECT `player_id`, `start_time`, `duration`, `reason` FROM `player_punishments` WHERE `player_id`=? AND `punishment_type`=?";
	public static final String UPDATE_QUERY = "UPDATE `player_punishments` SET `duration`=? WHERE `player_id`=? AND `punishment_type`=?";
	public static final String REPLACE_QUERY = "REPLACE INTO `player_punishments` VALUES (?,?,?,?,?)";
	public static final String DELETE_QUERY = "DELETE FROM `player_punishments` WHERE `player_id`=? AND `punishment_type`=?";

	public static void loadPlayerPunishments(Player player) {
		DB.select(SELECT_DURATIONS_QUERY, new ParamReadStH() {

			@Override
			public void setParams(PreparedStatement ps) throws SQLException {
				ps.setInt(1, player.getObjectId());
			}

			@Override
			public void handleRead(ResultSet rs) throws SQLException {
				while (rs.next()) {
					PunishmentType punishmentType = PunishmentType.valueOf(rs.getString("punishment_type"));
					if (punishmentType == PunishmentType.PRISON) {
						player.setPrisonEndTimeMillis(System.currentTimeMillis() + rs.getLong("duration") * 1000);
					} else if (punishmentType == PunishmentType.GATHER) {
						player.setGatherRestrictionExpirationTime(System.currentTimeMillis() + rs.getLong("duration") * 1000);
					}
				}
			}
		});
	}

	public static void storePlayerPunishment(Player player, PunishmentType punishmentType) {
		DB.insertUpdate(UPDATE_QUERY, new IUStH() {

			@Override
			public void handleInsertUpdate(PreparedStatement ps) throws SQLException {
				if (punishmentType == PunishmentType.PRISON) {
					ps.setLong(1, player.getPrisonDurationSeconds());
				} else if (punishmentType == PunishmentType.GATHER) {
					ps.setLong(1, player.getGatherRestrictionDurationSeconds());
				}
				ps.setInt(2, player.getObjectId());
				ps.setString(3, punishmentType.toString());
				ps.execute();
			}
		});
	}

	public static void punishPlayer(int playerId, PunishmentType punishmentType, long duration, String reason) {
		DB.insertUpdate(REPLACE_QUERY, new IUStH() {

			@Override
			public void handleInsertUpdate(PreparedStatement ps) throws SQLException {
				ps.setInt(1, playerId);
				ps.setString(2, punishmentType.toString());
				ps.setLong(3, System.currentTimeMillis() / 1000);
				ps.setLong(4, duration);
				ps.setString(5, reason);
				ps.execute();
			}
		});
	}

	public static void punishPlayer(Player player, PunishmentType punishmentType, String reason) {
		if (punishmentType == PunishmentType.PRISON)
			punishPlayer(player.getObjectId(), punishmentType, player.getPrisonDurationSeconds(), reason);
		else if (punishmentType == PunishmentType.GATHER)
			punishPlayer(player.getObjectId(), punishmentType, player.getGatherRestrictionDurationSeconds(), reason);
	}

	public static void unpunishPlayer(int playerId, PunishmentType punishmentType) {
		DB.insertUpdate(DELETE_QUERY, new IUStH() {

			@Override
			public void handleInsertUpdate(PreparedStatement ps) throws SQLException {
				ps.setInt(1, playerId);
				ps.setString(2, punishmentType.toString());
				ps.execute();
			}
		});
	}

	public static CharacterBanInfo getCharBanInfo(int playerId) {
		CharacterBanInfo[] charBan = new CharacterBanInfo[1];
		DB.select(SELECT_QUERY, new ParamReadStH() {

			@Override
			public void setParams(PreparedStatement ps) throws SQLException {
				ps.setInt(1, playerId);
				ps.setString(2, PunishmentType.CHARBAN.toString());
			}

			@Override
			public void handleRead(ResultSet rs) throws SQLException {
				while (rs.next()) {
					charBan[0] = new CharacterBanInfo(playerId, rs.getLong("start_time"), rs.getLong("duration"), rs.getString("reason"));
				}
			}
		});
		return charBan[0];
	}

}
