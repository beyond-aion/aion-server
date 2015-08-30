package com.aionemu.gameserver.dao;

import com.aionemu.commons.database.dao.DAO;
import com.aionemu.gameserver.model.account.Account;
import com.aionemu.gameserver.model.gameobjects.player.passport.Passport;
import java.util.List;


/**
 * @author Luzien
 */
public abstract class AccountPassportsDAO implements DAO {

    @Override
    public String getClassName() {
        return AccountPassportsDAO.class.getName();
    }

    public abstract void load(final Account account);

    public abstract void store(final int accountId, List<Passport> pList);
    
    public abstract void resetAllPassports();
    
    public abstract void resetAllStamps();
	
	public abstract void store(Account account);

}
