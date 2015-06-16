package com.aionemu.loginserver.dao;

import java.sql.Timestamp;
import java.util.Map;

import com.aionemu.commons.database.dao.DAO;


/**
 * @author ViAl
 *
 */
public abstract class BannedHddDAO implements DAO {
	
	public abstract boolean update(String serial, Timestamp time);

	public abstract boolean remove(String serial);

	public abstract Map<String, Timestamp> load();

	public abstract void cleanExpiredBans();
	
	@Override
	public final String getClassName() {
		return BannedHddDAO.class.getName();
	}
}
