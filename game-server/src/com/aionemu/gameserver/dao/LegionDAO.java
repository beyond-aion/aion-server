package com.aionemu.gameserver.dao;

import java.sql.Timestamp;
import java.util.TreeMap;

import com.aionemu.gameserver.model.team.legion.Legion;
import com.aionemu.gameserver.model.team.legion.LegionEmblem;
import com.aionemu.gameserver.model.team.legion.LegionHistory;
import com.aionemu.gameserver.model.team.legion.LegionWarehouse;

/**
 * Class that is responsible for storing/loading legion data
 * 
 * @author Simple
 */

public abstract class LegionDAO implements IDFactoryAwareDAO {

	/**
	 * Returns true if name is used, false in other case
	 * 
	 * @param name
	 *          name to check
	 * @return true if name is used, false in other case
	 */
	public abstract boolean isNameUsed(String name);

	/**
	 * Creates legion in DB
	 * 
	 * @param legion
	 */
	public abstract boolean saveNewLegion(Legion legion);

	/**
	 * Stores legion to DB
	 * 
	 * @param legion
	 */
	public abstract void storeLegion(Legion legion);

	/**
	 * Loads a legion
	 * 
	 * @param legionName
	 * @return
	 */
	public abstract Legion loadLegion(String legionName);

	/**
	 * Loads a legion
	 * 
	 * @param legionId
	 * @return Legion
	 */
	public abstract Legion loadLegion(int legionId);

	/**
	 * Removes legion and all related data (Done by CASCADE DELETION)
	 * 
	 * @param legionId
	 *          legion to delete
	 */
	public abstract void deleteLegion(int legionId);

	/**
	 * Returns the announcement list of a legion
	 * 
	 * @param legion
	 * @return announcementList
	 */
	public abstract TreeMap<Timestamp, String> loadAnnouncementList(int legionId);

	/**
	 * Creates announcement in DB
	 * 
	 * @param legionId
	 * @param currentTime
	 * @param message
	 * @return true or false
	 */
	public abstract boolean saveNewAnnouncement(int legionId, Timestamp currentTime, String message);

	/**
	 * Identifier name for all LegionDAO classes
	 * 
	 * @return LegionDAO.class.getName()
	 */
	@Override
	public final String getClassName() {
		return LegionDAO.class.getName();
	}

	/**
	 * Stores a legion emblem in the database
	 * 
	 * @param legionId
	 * @param emblemId
	 * @param red
	 * @param green
	 * @param blue
	 */
	public abstract void storeLegionEmblem(int legionId, LegionEmblem legionEmblem);

	/**
	 * @param legionId
	 * @param key
	 * @return
	 */
	public abstract void removeAnnouncement(int legionId, Timestamp key);

	/**
	 * Loads a legion emblem
	 * 
	 * @param legion
	 * @return LegionEmblem
	 */
	public abstract LegionEmblem loadLegionEmblem(int legionId);

	/**
	 * Loads the warehouse of legions
	 * 
	 * @param legion
	 * @return Storage
	 */
	public abstract LegionWarehouse loadLegionStorage(Legion legion);

	/**
	 * @param legion
	 */
	public abstract void loadLegionHistory(Legion legion);

	/**
	 * @param legionId
	 * @param legionHistory
	 * @return true if query successful
	 */
	public abstract boolean saveNewLegionHistory(int legionId, LegionHistory legionHistory);
}
