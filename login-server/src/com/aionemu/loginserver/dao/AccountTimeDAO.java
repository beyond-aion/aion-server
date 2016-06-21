package com.aionemu.loginserver.dao;

import com.aionemu.commons.database.dao.DAO;
import com.aionemu.loginserver.model.AccountTime;

/**
 * DAO to manage account time
 */
public abstract class AccountTimeDAO implements DAO {

	/**
	 * Updates {@link AccountTime} data of account
	 * 
	 * @param accountId
	 *          account id
	 * @param accountTime
	 *          account time set
	 * @return Update success status
	 */
	public abstract boolean updateAccountTime(int accountId, AccountTime accountTime);

	/**
	 * Updates {@link AccountTime} data of account
	 * 
	 * @param accountId
	 * @return AccountTime or null on error
	 */
	public abstract AccountTime getAccountTime(int accountId);

	/**
	 * Returns unique class name for all implementations
	 * 
	 * @return unique class name for all implementations
	 */
	@Override
	public final String getClassName() {
		return AccountTimeDAO.class.getName();
	}

}
