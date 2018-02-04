package com.aionemu.gameserver.dao;

import java.util.Map;
import java.util.Set;

import com.aionemu.commons.database.dao.DAO;

/**
 * @author ViAl
 */
public abstract class CommandsAccessDAO implements DAO {

	public abstract Map<Integer, Set<String>> loadAccesses();

	public abstract void addAccess(int playerId, String commandName);

	public abstract void removeAccess(int playerId, String commandName);

	public abstract void removeAllAccesses(int playerId);

	@Override
	public String getClassName() {
		return CommandsAccessDAO.class.getName();
	}

}
