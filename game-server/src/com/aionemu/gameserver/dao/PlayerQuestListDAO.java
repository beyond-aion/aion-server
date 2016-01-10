package com.aionemu.gameserver.dao;

import com.aionemu.commons.database.dao.DAO;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.player.QuestStateList;

/**
 * @author MrPoke
 * @modified vlog
 */
public abstract class PlayerQuestListDAO implements DAO {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getClassName() {
		return PlayerQuestListDAO.class.getName();
	}

	/**
	 * @param playerId
	 * @return QuestStateList
	 */
	public abstract QuestStateList load(int playerObjId);

	/**
	 * @param Player
	 * @param QuestStateList
	 */
	public abstract void store(final Player player);
}
