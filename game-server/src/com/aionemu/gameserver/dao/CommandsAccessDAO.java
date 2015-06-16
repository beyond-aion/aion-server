package com.aionemu.gameserver.dao;

import java.util.List;
import java.util.Map;

import com.aionemu.commons.database.dao.DAO;


/**
 * @author ViAl
 *
 */
public abstract class CommandsAccessDAO  implements DAO {

	public abstract Map<Integer, List<String>> loadAccesses();
	
	public abstract void addAccess(int playerId, String commandName);
	
	public abstract void removeAccess(int playerId, String commandName);
	
	public abstract void removeAllAccesses(int playerId);
	
	@Override
	public String getClassName() {
		return CommandsAccessDAO.class.getName();
	}
	
}
