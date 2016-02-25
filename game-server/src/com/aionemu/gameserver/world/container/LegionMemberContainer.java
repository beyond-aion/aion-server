package com.aionemu.gameserver.world.container;

import java.util.concurrent.ConcurrentHashMap;

import com.aionemu.commons.database.dao.DAOManager;
import com.aionemu.gameserver.dao.LegionMemberDAO;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.team.legion.LegionMember;
import com.aionemu.gameserver.model.team.legion.LegionMemberEx;
import com.aionemu.gameserver.world.exceptions.DuplicateAionObjectException;

/**
 * Container for storing Legion members by Id and name.
 * 
 * @author Simple
 */
public class LegionMemberContainer {

	private final ConcurrentHashMap<Integer, LegionMember> legionMemberById = new ConcurrentHashMap<>();

	private final ConcurrentHashMap<Integer, LegionMemberEx> legionMemberExById = new ConcurrentHashMap<>();
	private final ConcurrentHashMap<String, LegionMemberEx> legionMemberExByName = new ConcurrentHashMap<>();

	/**
	 * Add LegionMember to this Container.
	 * 
	 * @param legionMember
	 */
	public void addMember(LegionMember legionMember) {
		if (!legionMemberById.containsKey(legionMember.getObjectId()))
			legionMemberById.putIfAbsent(legionMember.getObjectId(), legionMember);
	}

	/**
	 * This method will return a member from cache
	 * 
	 * @param memberObjId
	 */
	public LegionMember getMember(int memberObjId) {
		return legionMemberById.get(memberObjId);
	}

	/**
	 * Add LegionMemberEx to this Container.
	 * 
	 * @param legionMember
	 */
	public void addMemberEx(LegionMemberEx legionMember) {
		if (legionMemberExById.containsKey(legionMember.getObjectId()) || legionMemberExByName.containsKey(legionMember.getName()))
			throw new DuplicateAionObjectException();
		legionMemberExById.put(legionMember.getObjectId(), legionMember);
		legionMemberExByName.put(legionMember.getName(), legionMember);
	}

	/**
	 * This method will return a memberEx from cache
	 * 
	 * @param memberObjId
	 */
	public LegionMemberEx getMemberEx(int memberObjId) {
		return legionMemberExById.get(memberObjId);
	}

	/**
	 * This method will return a memberEx from cache
	 * 
	 * @param memberName
	 */
	public LegionMemberEx getMemberEx(String memberName) {
		return legionMemberExByName.get(memberName);
	}

	/**
	 * Remove LegionMember from this Container.
	 * 
	 * @param legionMember
	 */
	public void remove(LegionMemberEx legionMember) {
		legionMemberById.remove(legionMember.getObjectId());
		legionMemberExById.remove(legionMember.getObjectId());
		legionMemberExByName.remove(legionMember.getName());
	}

	/**
	 * Returns true if legion is in cached by id
	 * 
	 * @param memberObjId
	 * @return true or false
	 */
	public boolean contains(int memberObjId) {
		return legionMemberById.containsKey(memberObjId);
	}

	/**
	 * Returns true if legion is in cached by id
	 * 
	 * @param memberObjId
	 * @return true or false
	 */
	public boolean containsEx(int memberObjId) {
		return legionMemberExById.containsKey(memberObjId);
	}

	/**
	 * Returns true if legion is in cached by id
	 * 
	 * @param memberName
	 * @return true or false
	 */
	public boolean containsEx(String memberName) {
		return legionMemberExByName.containsKey(memberName);
	}

	public void clear() {
		legionMemberById.clear();
		legionMemberExById.clear();
		legionMemberExByName.clear();
	}
	
	/**
	 * for name changes
	 * @param player
	 */
	public void remove(Player player) {
		if (player == null) {
			return;
		}
		if (legionMemberById.containsKey(player)) {
			legionMemberById.remove(player);
		}
		if (legionMemberExById.containsKey(player)) {
			legionMemberExById.remove(player);
		}
		if (legionMemberExByName.containsKey(player.getName())) {
			legionMemberExByName.remove(player.getName());
		}
	}
	
	/**
	 * for name changes
	 * @param player
	 */
	public void add(Player player) {
		if (player == null) {
			return;
		}
		if (!legionMemberById.containsKey(player)) {
			LegionMember legionMember = DAOManager.getDAO(LegionMemberDAO.class).loadLegionMember(player.getObjectId());
			if (legionMember != null) {
				legionMemberById.putIfAbsent(legionMember.getObjectId(), legionMember);
			}
		}
		if (!legionMemberExById.containsKey(player)) {
			LegionMemberEx legionMember = DAOManager.getDAO(LegionMemberDAO.class).loadLegionMemberEx(player.getObjectId());
			if (legionMember != null) {
				legionMemberExById.putIfAbsent(legionMember.getObjectId(), legionMember);
			}
		}
		if (!legionMemberExByName.containsKey(player.getName())) {
			LegionMemberEx legionMember = DAOManager.getDAO(LegionMemberDAO.class).loadLegionMemberEx(player.getObjectId());
			if (legionMember != null) {
				legionMemberExByName.putIfAbsent(legionMember.getName(), legionMember);
			}
		}
	}
}
