package com.aionemu.gameserver.dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.database.DatabaseFactory;
import com.aionemu.gameserver.model.gameobjects.player.PetCommonData;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.pet.PetDopingBag;
import com.aionemu.gameserver.services.toypet.PetHungryLevel;

/**
 * @author Xitanium, Kamui, Rolandas, M@xx, xTz
 */
public class PlayerPetsDAO {

	private static final Logger log = LoggerFactory.getLogger(PlayerPetsDAO.class);

	public static void saveFeedStatus(int petObjectId, int hungryLevel, int feedProgress, long reuseTime) {
		try (Connection con = DatabaseFactory.getConnection();
				 PreparedStatement stmt = con.prepareStatement("UPDATE player_pets SET hungry_level = ?, feed_progress = ?, reuse_time = ? WHERE id = ?")) {
			stmt.setInt(1, hungryLevel);
			stmt.setInt(2, feedProgress);
			stmt.setLong(3, reuseTime);
			stmt.setInt(4, petObjectId);
			stmt.execute();
		} catch (Exception e) {
			log.error("Error updating feed status for pet #" + petObjectId, e);
		}
	}

	public static void saveDopingBag(int petObjectId, PetDopingBag bag) {
		try (Connection con = DatabaseFactory.getConnection();
				 PreparedStatement stmt = con.prepareStatement("UPDATE player_pets SET dopings = ? WHERE id = ?")) {
			String itemIds = bag.getFoodItem() + "," + bag.getDrinkItem();
			for (int itemId : bag.getScrollsUsed())
				itemIds += "," + itemId;
			stmt.setString(1, itemIds);
			stmt.setInt(2, petObjectId);
			stmt.execute();
		} catch (Exception e) {
			log.error("Error update doping for pet #" + petObjectId, e);
		}
	}

	public static void setTime(int petObjectId, long time) {
		try (Connection con = DatabaseFactory.getConnection();
				 PreparedStatement stmt = con.prepareStatement("UPDATE player_pets SET reuse_time = ? WHERE id = ?")) {
			stmt.setLong(1, time);
			stmt.setInt(2, petObjectId);
			stmt.execute();
		} catch (Exception e) {
			log.error("Error update pet #" + petObjectId, e);
		}
	}

	public static void insertPlayerPet(Player player, PetCommonData petCommonData) {
		try (Connection con = DatabaseFactory.getConnection();
				 PreparedStatement stmt = con.prepareStatement(
					 "INSERT INTO player_pets(id, player_id, template_id, decoration, name, despawn_time, expire_time) VALUES(?, ?, ?, ?, ?, ?, ?)")) {
			stmt.setInt(1, petCommonData.getObjectId());
			stmt.setInt(2, player.getObjectId());
			stmt.setInt(3, petCommonData.getTemplateId());
			stmt.setInt(4, petCommonData.getDecoration());
			stmt.setString(5, petCommonData.getName());
			stmt.setTimestamp(6, petCommonData.getDespawnTime());
			stmt.setInt(7, petCommonData.getExpireTime());
			stmt.execute();
		} catch (Exception e) {
			log.error("Error inserting new pet #" + petCommonData.getObjectId() + ", name: " + petCommonData.getName(), e);
		}
	}

	public static void removePlayerPet(int petObjectId) {
		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement stmt = con.prepareStatement("DELETE FROM player_pets WHERE id = ?")) {
			stmt.setInt(1, petObjectId);
			stmt.execute();
		} catch (Exception e) {
			log.error("Error removing pet #" + petObjectId, e);
		}
	}

	public static List<PetCommonData> getPlayerPets(Player player) {
		List<PetCommonData> pets = new ArrayList<>();
		try (Connection con = DatabaseFactory.getConnection();
				 PreparedStatement stmt = con.prepareStatement("SELECT * FROM player_pets WHERE player_id = ?")) {
			stmt.setInt(1, player.getObjectId());
			try (ResultSet rs = stmt.executeQuery()) {
				while (rs.next()) {
					PetCommonData petCommonData = new PetCommonData(rs.getInt("id"), rs.getInt("template_id"), player.getObjectId(),
						rs.getInt("expire_time"));
					petCommonData.setName(rs.getString("name"));
					petCommonData.setDecoration(rs.getInt("decoration"));
					if (petCommonData.getFeedProgress() != null) {
						petCommonData.getFeedProgress().setHungryLevel(PetHungryLevel.fromId(rs.getInt("hungry_level")));
						petCommonData.getFeedProgress().setData(rs.getInt("feed_progress"));
						petCommonData.setRefeedTime(rs.getLong("reuse_time"));
					}
					if (petCommonData.getDopingBag() != null) {
						String dopings = rs.getString("dopings");
						if (dopings != null) {
							String[] ids = dopings.split(",");
							for (int i = 0; i < ids.length; i++)
								petCommonData.getDopingBag().setItem(Integer.parseInt(ids[i]), i);
						}
					}
					petCommonData.setBirthday(rs.getTimestamp("birthday"));
					petCommonData.setStartMoodTime(rs.getLong("mood_started"));
					petCommonData.setShuggleCounter(rs.getInt("counter"));
					petCommonData.setMoodCdStarted(rs.getLong("mood_cd_started"));
					petCommonData.setGiftCdStarted(rs.getLong("gift_cd_started"));
					Timestamp ts = rs.getTimestamp("despawn_time");
					if (ts == null)
						ts = new Timestamp(System.currentTimeMillis());
					petCommonData.setDespawnTime(ts);
					pets.add(petCommonData);
				}
			}
		} catch (Exception e) {
			log.error("Error loading pets for " + player, e);
		}
		return pets;
	}

	public static void updatePetName(PetCommonData petCommonData) {
		try (Connection con = DatabaseFactory.getConnection();
				 PreparedStatement stmt = con.prepareStatement("UPDATE player_pets SET name = ? WHERE id = ?")) {
			stmt.setString(1, petCommonData.getName());
			stmt.setInt(2, petCommonData.getObjectId());
			stmt.execute();
		} catch (Exception e) {
			log.error("Error update pet #" + petCommonData.getObjectId(), e);
		}
	}

	public static boolean savePetMoodData(PetCommonData petCommonData) {
		try (Connection con = DatabaseFactory.getConnection();
				 PreparedStatement stmt = con.prepareStatement(
					 "UPDATE player_pets SET mood_started = ?, counter = ?, mood_cd_started = ?, gift_cd_started = ?, despawn_time = ? WHERE id = ?")) {
			stmt.setLong(1, petCommonData.getMoodStartTime());
			stmt.setInt(2, petCommonData.getShuggleCounter());
			stmt.setLong(3, petCommonData.getMoodCdStarted());
			stmt.setLong(4, petCommonData.getGiftCdStarted());
			stmt.setTimestamp(5, petCommonData.getDespawnTime());
			stmt.setInt(6, petCommonData.getObjectId());
			stmt.execute();
		} catch (Exception e) {
			log.error("Error updating mood for pet #" + petCommonData.getObjectId(), e);
			return false;
		}
		return true;
	}

	public static int[] getUsedIDs() {
		try (Connection con = DatabaseFactory.getConnection();
				 PreparedStatement stmt = con.prepareStatement("SELECT id FROM player_pets", ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY)) {
			ResultSet rs = stmt.executeQuery();
			rs.last();
			int count = rs.getRow();
			rs.beforeFirst();
			int[] ids = new int[count];
			for (int i = 0; rs.next(); i++)
				ids[i] = rs.getInt("id");
			return ids;
		} catch (SQLException e) {
			log.error("Can't get list of IDs from pets table", e);
			return null;
		}
	}

}
