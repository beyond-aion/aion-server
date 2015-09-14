package com.aionemu.gameserver.dao;

import java.util.List;

import com.aionemu.commons.database.dao.DAO;
import com.aionemu.gameserver.model.account.Account;
import com.aionemu.gameserver.model.gameobjects.player.passport.Passport;

/**
 * @author Luzien
 */
public abstract class AccountPassportsDAO implements DAO {

	@Override
	public String getClassName() {
		return AccountPassportsDAO.class.getName();
	}

	public abstract void loadPassport(final Account account);

	public abstract void storePassport(final Account account);

	public abstract void storePassportList(final int accountId, List<Passport> pList);

	public abstract void resetAllPassports();

	public abstract void resetAllStamps();

}
