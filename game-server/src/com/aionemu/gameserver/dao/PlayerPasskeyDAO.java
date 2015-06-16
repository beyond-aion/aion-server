package com.aionemu.gameserver.dao;

import com.aionemu.commons.database.dao.DAO;

/**
 * @author cura
 */
public abstract class PlayerPasskeyDAO implements DAO {

	/**
	 * @param accountId
	 * @param passkey
	 */
	public abstract void insertPlayerPasskey(int accountId, String passkey);

	/**
	 * @param accountId
	 * @param oldPasskey
	 * @param newPasskey
	 * @return
	 */
	public abstract boolean updatePlayerPasskey(int accountId, String oldPasskey, String newPasskey);

	/**
	 * @param accountId
	 * @param newPasskey
	 * @return
	 */
	public abstract boolean updateForcePlayerPasskey(int accountId, String newPasskey);

	/**
	 * @param accountId
	 * @param passkey
	 * @return
	 */
	public abstract boolean checkPlayerPasskey(int accountId, String passkey);

	/**
	 * @param accountId
	 * @return
	 */
	public abstract boolean existCheckPlayerPasskey(int accountId);

	/*
	 * (non-Javadoc)
	 * @see com.aionemu.commons.database.dao.DAO#getClassName()
	 */
	@Override
	public final String getClassName() {
		return PlayerPasskeyDAO.class.getName();
	}
}
