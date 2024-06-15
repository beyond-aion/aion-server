package com.aionemu.gameserver.world.container;

import java.util.concurrent.ConcurrentHashMap;

import com.aionemu.gameserver.dao.LegionMemberDAO;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.team.legion.LegionMember;
import com.aionemu.gameserver.model.team.legion.LegionMemberEx;

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
			throw new IllegalStateException(legionMember.getName() + " is already cached");
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
	 */
	public void remove(int legionMemberObjId) {
		legionMemberById.remove(legionMemberObjId);
		LegionMemberEx legionMemberEx = legionMemberExById.remove(legionMemberObjId);
		if (legionMemberEx != null)
			legionMemberExByName.remove(legionMemberEx.getName());
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

	public void updateCachedPlayerName(String oldName, Player player) {
		legionMemberExByName.compute(oldName, (n, legionMember) -> {
			if (legionMember == null)
				legionMember = LegionMemberDAO.loadLegionMemberEx(player.getObjectId());
			else
				legionMember.setName(player.getName());
			legionMemberExByName.put(player.getName(), legionMember);
			return null;
		});
	}
}
