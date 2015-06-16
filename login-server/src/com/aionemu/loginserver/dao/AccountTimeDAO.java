package com.aionemu.loginserver.dao;

import com.aionemu.commons.database.dao.DAO;
import com.aionemu.loginserver.model.AccountTime;

/**
 * DAO to manage account time
 */
public abstract class AccountTimeDAO implements DAO {

	/**
	 * Updates @link com.aionemu.loginserver.model.AccountTime data of account
	 * 
	 * @param accountId
	 *          account id
	 * @param accountTime
	 *          account time set
	 * @return was update successfull or not
	 */
	public abstract boolean updateAccountTime(int accountId, AccountTime accountTime);

	/**
	 * Updates @link com.aionemu.loginserver.model.AccountTime data of account
	 * 
	 * @param accountId
	 * @return AccountTime
	 */
	public abstract AccountTime getAccountTime(int accountId);

	/**
	 * Returns uniquire class name for all implementations
	 * 
	 * @return uniquire class name for all implementations
	 */
	@Override
	public final String getClassName() {
		return AccountTimeDAO.class.getName();
	}

}
