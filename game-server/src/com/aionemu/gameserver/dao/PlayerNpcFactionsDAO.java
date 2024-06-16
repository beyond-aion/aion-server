package com.aionemu.gameserver.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.database.DatabaseFactory;
import com.aionemu.gameserver.model.gameobjects.Persistable.PersistentState;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.player.npcFaction.ENpcFactionQuestState;
import com.aionemu.gameserver.model.gameobjects.player.npcFaction.NpcFaction;
import com.aionemu.gameserver.model.gameobjects.player.npcFaction.NpcFactions;

/**
 * @author MrPoke
 */
public class PlayerNpcFactionsDAO {

	private static final Logger log = LoggerFactory.getLogger(PlayerNpcFactionsDAO.class);

	public static final String SELECT_QUERY = "SELECT `faction_id`, `active`, `time`, `state`, `quest_id` FROM player_npc_factions WHERE `player_id`=?";
	public static final String INSERT_QUERY = "INSERT INTO player_npc_factions (`player_id`, `faction_id`, `active`, `time`, `state`, `quest_id`) VALUES (?,?,?,?,?,?)";
	public static final String UPDATE_QUERY = "UPDATE player_npc_factions SET `active`=?, `time`=?, `state`=?, `quest_id`=?  WHERE `player_id`=? AND `faction_id`=?";

	public static void loadNpcFactions(Player player) {
		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement stmt = con.prepareStatement(SELECT_QUERY)) {
			stmt.setInt(1, player.getObjectId());
			try (ResultSet rset = stmt.executeQuery()) {
				NpcFactions factions = new NpcFactions(player);
				player.setNpcFactions(factions);
				while (rset.next()) {
					int faction_id = rset.getInt("faction_id");
					boolean active = rset.getBoolean("active");
					int time = rset.getInt("time");
					int questId = rset.getInt("quest_id");
					ENpcFactionQuestState state = ENpcFactionQuestState.valueOf(rset.getString("state"));
					NpcFaction faction = new NpcFaction(faction_id, time, active, state, questId);
					faction.setPersistentState(PersistentState.UPDATED);
					factions.addNpcFaction(faction);
				}
			}
		} catch (Exception e) {
			log.error("Could not restore Npc faction data for playerObjId: " + player.getObjectId() + " from DB: " + e.getMessage(), e);
		}
	}

	public static void storeNpcFactions(Player player) {
		for (NpcFaction npcFaction : player.getNpcFactions().getNpcFactions()) {
			switch (npcFaction.getPersistentState()) {
				case NEW:
					insertNpcFaction(player.getObjectId(), npcFaction);
					break;
				case UPDATE_REQUIRED:
					updateNpcFaction(player.getObjectId(), npcFaction);
					break;
			}
		}
	}

	private static void insertNpcFaction(int playerObjectId, NpcFaction faction) {
		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement stmt = con.prepareStatement(INSERT_QUERY)) {
			stmt.setInt(1, playerObjectId);
			stmt.setInt(2, faction.getId());
			stmt.setBoolean(3, faction.isActive());
			stmt.setInt(4, faction.getTime());
			stmt.setString(5, faction.getState().name());
			stmt.setInt(6, faction.getQuestId());
			stmt.execute();
		} catch (Exception e) {
			log.error("Could not insert Npc faction data for playerObjId: " + playerObjectId + " from DB: " + e.getMessage(), e);
		}
	}

	private static void updateNpcFaction(int playerObjectId, NpcFaction faction) {
		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement stmt = con.prepareStatement(UPDATE_QUERY)) {
			stmt.setBoolean(1, faction.isActive());
			stmt.setInt(2, faction.getTime());
			stmt.setString(3, faction.getState().name());
			stmt.setInt(4, faction.getQuestId());
			stmt.setInt(5, playerObjectId);
			stmt.setInt(6, faction.getId());
			stmt.execute();
		} catch (Exception e) {
			log.error("Could not update Npc faction data for playerObjId: " + playerObjectId + " from DB: " + e.getMessage(), e);
		}
	}

}
