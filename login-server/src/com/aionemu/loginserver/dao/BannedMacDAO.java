package com.aionemu.loginserver.dao;

import java.util.Map;

import com.aionemu.commons.database.dao.DAO;
import com.aionemu.loginserver.model.base.BannedMacEntry;

/**
 * 
 * @author KID
 *
 */
public abstract class BannedMacDAO implements DAO {
	public abstract boolean update(BannedMacEntry entry);

	public abstract boolean remove(String address);

	public abstract Map<String, BannedMacEntry> load();

	public abstract void cleanExpiredBans();

	@Override
	public final String getClassName() {
		return BannedMacDAO.class.getName();
	}
}
