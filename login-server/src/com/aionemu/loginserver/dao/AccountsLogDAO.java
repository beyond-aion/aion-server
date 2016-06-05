package com.aionemu.loginserver.dao;

import com.aionemu.commons.database.dao.DAO;

/**
 * @author ViAl
 */
public abstract class AccountsLogDAO implements DAO {

	public abstract void addRecord(int accountId, byte gameserverId, long time, String ip, String mac, String hddSerial);

	public final String getClassName() {
		return AccountsLogDAO.class.getName();
	}
}
