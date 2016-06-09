package com.aionemu.gameserver.dao;

import java.util.Map;

import com.aionemu.commons.database.dao.DAO;
import com.aionemu.gameserver.model.event.Headhunter;

/**
 * Created on 30.05.2016
 * 
 * @author Estrayl
 * @since AION 4.8
 */
public abstract class HeadhuntingDAO implements DAO {

	public abstract Map<Integer, Headhunter> loadHeadhunters();
	
	public abstract boolean clearTables();

	public abstract void storeHeadhunter(int hunterId);

	@Override
	public String getClassName() {
		return HeadhuntingDAO.class.getName();
	}
}
