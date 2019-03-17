package com.aionemu.gameserver.dao;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.aionemu.commons.database.dao.DAO;
import com.aionemu.gameserver.custom.instance.neuralnetwork.PlayerModelEntry;

/**
 * @author Jo
 */
public abstract class CustomInstancePlayerModelEntryDAO implements DAO {

	public abstract Map<Integer, List<PlayerModelEntry>> loadPlayerModelEntries();

	public abstract void insertNewRecords(Collection<PlayerModelEntry> filteredEntries);

	@Override
	public String getClassName() {
		return CustomInstancePlayerModelEntryDAO.class.getName();
	}

}
