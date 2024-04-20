package com.aionemu.gameserver.dao;

import java.sql.Timestamp;
import java.util.List;
import java.util.Set;

import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.account.PlayerAccountData;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.player.PlayerCommonData;

/**
 * Class that is responsible for storing/loading player data
 *
 * @author SoulKeeper, Saelya, cura
 */
public abstract class PlayerDAO implements IDFactoryAwareDAO {

	public abstract boolean isNameUsed(String name);

	public abstract void storePlayer(Player player);

	public abstract boolean saveNewPlayer(Player player, int accountId, String accountName);

	public abstract PlayerCommonData loadPlayerCommonData(int playerObjId);

	/**
	 * Removes player and all related data (Done by CASCADE DELETION)
	 */
	public abstract void deletePlayer(int playerId);

	public abstract void updateDeletionTime(int objectId, Timestamp deletionDate);

	public abstract void storeCreationTime(int objectId, Timestamp creationDate);

	/**
	 * Loads creation and deletion time from database, for particular player and sets these values in given <tt>PlayerAccountData</tt> object.
	 */
	public abstract void setCreationDeletionTime(PlayerAccountData acData);

	/**
	 * Returns a list of objectId of players that are on the account with given accountId
	 */
	public abstract List<Integer> getPlayerOidsOnAccount(int accountId);

	/**
	 * Returns a list of objectIds of players that are on the account with given accountId and exp <= maxExp
	 */
	public abstract List<Integer> getPlayerOidsOnAccount(int accountId, long maxExp);

	/**
	 * Stores the last online time
	 *
	 * @param objectId
	 *          Object ID of player to store
	 * @param lastOnline
	 *          Last online time of player to store
	 */
	public abstract void storeLastOnlineTime(final int objectId, final Timestamp lastOnline);

	/**
	 * @return True if the player is marked as online in DB, false otherwise or if player wasn't found.
	 */
	public abstract boolean isOnline(int playerId);

	/**
	 * Store online or offline player status
	 */
	public abstract void onlinePlayer(final Player player, final boolean online);

	public abstract void setAllPlayersOffline();

	public abstract PlayerCommonData loadPlayerCommonDataByName(String name);

	public abstract int getAccountIdByName(final String name);

	public abstract int getAccountId(final int playerId);

	public abstract String getPlayerNameByObjId(final int playerObjId);

	public abstract int getPlayerIdByName(final String playerName);

	public abstract void storePlayerName(PlayerCommonData recipientCommonData);

	public abstract int getCharacterCountOnAccount(final int accountId);

	public abstract int getCharacterCountForRace(Race race);

	/**
	 * @param daysOfInactivity
	 *          Number of days a char needs to be inactive
	 * @return objectIds of accounts that are inactive for more than dayOfInactivity days
	 */
	public abstract Set<Integer> getInactiveAccounts(final int daysOfInactivity);

	public abstract void setPlayerLastTransferTime(final int playerId, final long time);

	/**
	 * Returns the character level of his last session.
	 * 
	 * @return Old level, 0 on first DB initialization if there were already players in the table.
	 * @see #storeOldCharacterLevel(int, int)
	 */
	public abstract int getOldCharacterLevel(int playerObjectId);

	/**
	 * Saves the last known character level.
	 * 
	 * @see #getOldCharacterLevel(int)
	 */
	public abstract void storeOldCharacterLevel(int playerObjectId, int level);

	@Override
	public final String getClassName() {
		return PlayerDAO.class.getName();
	}
}
