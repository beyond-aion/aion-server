package com.aionemu.gameserver.dao;

import java.util.List;

import com.aionemu.commons.database.dao.DAO;
import com.aionemu.gameserver.model.team.legion.LegionMember;
import com.aionemu.gameserver.model.team.legion.LegionMemberEx;

/**
 * Class that is responsible for storing/loading legion data
 * 
 * @author Simple
 */

public abstract class LegionMemberDAO implements DAO {

	/**
	 * Returns true if name is used, false in other case
	 * 
	 * @param name
	 *          name to check
	 * @return true if name is used, false in other case
	 */
	public abstract boolean isIdUsed(int playerObjId);

	/**
	 * Creates legion member in DB
	 * 
	 * @param legionMember
	 */
	public abstract boolean saveNewLegionMember(LegionMember legionMember);

	/**
	 * Stores legion member to DB
	 * 
	 * @param player
	 */
	public abstract void storeLegionMember(int playerObjId, LegionMember legionMember);

	/**
	 * Loads a legion member
	 * 
	 * @param playerObjId
	 * @param legionService
	 * @return LegionMember
	 */
	public abstract LegionMember loadLegionMember(int playerObjId);

	/**
	 * Loads an off line legion member by id
	 * 
	 * @param playerObjId
	 * @param legionService
	 * @return LegionMemberEx
	 */
	public abstract LegionMemberEx loadLegionMemberEx(int playerObjId);

	/**
	 * Loads an off line legion member by name
	 * 
	 * @param playerName
	 * @param legionService
	 * @return LegionMemberEx
	 */
	public abstract LegionMemberEx loadLegionMemberEx(String playerName);

	/**
	 * Loads all legion members of a legion
	 * 
	 * @param legionId
	 * @return List<Integer>
	 */
	public abstract List<Integer> loadLegionMembers(int legionId);

	/**
	 * Removes legion member and all related data (Done by CASCADE DELETION)
	 * 
	 * @param playerId
	 *          legion member to delete
	 */
	public abstract void deleteLegionMember(int playerObjId);

	/**
	 * Identifier name for all LegionDAO classes
	 * 
	 * @return LegionDAO.class.getName()
	 */
	@Override
	public final String getClassName() {
		return LegionMemberDAO.class.getName();
	}

}
