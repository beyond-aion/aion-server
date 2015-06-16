package com.aionemu.loginserver.dao;

import javolution.util.FastList;
import com.aionemu.commons.database.dao.DAO;
import com.aionemu.loginserver.service.ptransfer.PlayerTransferTask;

/**
 * @author KID
 */
public abstract class PlayerTransferDAO implements DAO {
	public abstract FastList<PlayerTransferTask> getNew();
	
	public abstract boolean update(PlayerTransferTask task);

	@Override
	public final String getClassName() {
		return PlayerTransferDAO.class.getName();
	}
}
