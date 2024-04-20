package com.aionemu.gameserver.dao;

import com.aionemu.commons.database.dao.DAO;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.player.QuestStateList;

/**
 * @author MrPoke, vlog
 */
public abstract class PlayerQuestListDAO implements DAO {

	@Override
	public String getClassName() {
		return PlayerQuestListDAO.class.getName();
	}

	public abstract QuestStateList load(int playerObjId);

	public abstract void store(final Player player);
}
