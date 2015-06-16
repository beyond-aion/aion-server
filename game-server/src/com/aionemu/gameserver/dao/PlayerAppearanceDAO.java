package com.aionemu.gameserver.dao;

//~--- non-JDK imports --------------------------------------------------------

import com.aionemu.commons.database.dao.DAO;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.player.PlayerAppearance;

/**
 * Class that is responsible for loading/storing player appearance
 * 
 * @author SoulKeeper
 */
public abstract class PlayerAppearanceDAO implements DAO {

	/**
	 * Returns unique identifier for PlayerAppearanceDAO
	 * 
	 * @return unique identifier for PlayerAppearanceDAO
	 */
	@Override
	public final String getClassName() {
		return PlayerAppearanceDAO.class.getName();
	}

	/**
	 * Loads player apperance DAO by player ID.<br>
	 * Returns null if not found in database
	 * 
	 * @param playerId
	 *          player id
	 * @return player appearance or null
	 */
	public abstract PlayerAppearance load(int playerId);

	/**
	 * Saves player appearance in database.<br>
	 * Actually calls {@link #store(int, com.aionemu.gameserver.model.gameobjects.player.PlayerAppearance)}
	 * 
	 * @param player
	 *          whos appearance to store
	 * @return true, if sql query was successful, false overwise
	 */
	public final boolean store(Player player) {
		return store(player.getObjectId(), player.getPlayerAppearance());
	}

	/**
	 * Stores appearance in database
	 * 
	 * @param id
	 *          player id
	 * @param playerAppearance
	 *          player appearance
	 * @return true, if sql query was successful, false overwise
	 */
	public abstract boolean store(int id, PlayerAppearance playerAppearance);
}
