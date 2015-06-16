package com.aionemu.gameserver.dao;

import com.aionemu.commons.database.dao.DAO;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.player.passport.PassportsList;


/**
 * @author Alcapwnd
 */
public abstract class PlayerPassportsDAO implements DAO {

    @Override
    public String getClassName() {
        return PlayerPassportsDAO.class.getName();
    }

    public abstract PassportsList load(final Player player);

    public abstract void store(final Player player);
    
    public abstract void resetAllPassports();
    
    public abstract void resetAllStamps();

}
