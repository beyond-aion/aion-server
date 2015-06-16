package com.aionemu.gameserver.dao;

import com.aionemu.commons.database.dao.DAO;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.player.title.Title;
import com.aionemu.gameserver.model.gameobjects.player.title.TitleList;

/**
 * @author xavier
 */
public abstract class PlayerTitleListDAO implements DAO {

	@Override
	public final String getClassName() {
		return PlayerTitleListDAO.class.getName();
	}

	public abstract TitleList loadTitleList(int playerId);

	public abstract boolean storeTitles(Player player, Title entry);
	
	public abstract boolean removeTitle(int playerId, int titleId);

}
