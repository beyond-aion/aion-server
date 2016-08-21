package com.aionemu.loginserver.dao;

import com.aionemu.commons.database.dao.DAO;
import com.aionemu.loginserver.service.ptransfer.PlayerTransferTask;

import javolution.util.FastTable;

/**
 * @author KID
 */
public abstract class PlayerTransferDAO implements DAO {

	public abstract FastTable<PlayerTransferTask> getNew();

	public abstract boolean update(PlayerTransferTask task);

	@Override
	public final String getClassName() {
		return PlayerTransferDAO.class.getName();
	}
}
