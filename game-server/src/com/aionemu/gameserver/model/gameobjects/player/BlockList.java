package com.aionemu.gameserver.model.gameobjects.player;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Represents a players list of blocked users<br />
 * Blocks via a player's CommonData
 * 
 * @author Ben
 */
public class BlockList implements Iterable<BlockedPlayer> {

	/**
	 * The maximum number of users a block list can contain
	 */
	public static final int MAX_BLOCKS = 100;

	// Indexes blocked players by their player ID
	private final Map<Integer, BlockedPlayer> blockedList;

	/**
	 * Constructs a new (empty) blocked list
	 */
	public BlockList() {
		this.blockedList = new ConcurrentHashMap<>();
	}

	/**
	 * Constructs a new blocked list with the given initial items
	 * 
	 * @param initialList
	 *          A map of blocked players indexed by their object IDs
	 */
	public BlockList(Map<Integer, BlockedPlayer> initialList) {
		this.blockedList = new ConcurrentHashMap<>(initialList);

	}

	/**
	 * Adds a player to the blocked users list<br />
	 * <ul>
	 * <li>Does not send packets or update the database</li>
	 * </ul>
	 * 
	 * @param playerToBlock
	 *          The player to be blocked
	 * @param reason
	 *          The reason for blocking this user
	 */
	public void add(BlockedPlayer plr) {
		blockedList.put(plr.getObjId(), plr);
	}

	/**
	 * Removes a player from the blocked users list<br />
	 * <ul>
	 * <li>Does not send packets or update the database</li>
	 * </ul>
	 * 
	 * @param objIdOfPlayer
	 */
	public void remove(int objIdOfPlayer) {
		blockedList.remove(objIdOfPlayer);
	}

	/**
	 * Returns the blocked player with this name if they exist
	 * 
	 * @param name
	 * @return CommonData of player with this name, null if not blocked
	 */
	public BlockedPlayer getBlockedPlayer(String name) {
		Iterator<BlockedPlayer> iterator = blockedList.values().iterator();

		while (iterator.hasNext()) {
			BlockedPlayer entry = iterator.next();
			if (entry.getName().equalsIgnoreCase(name))
				return entry;
		}
		return null;
	}

	public BlockedPlayer getBlockedPlayer(int playerObjId) {
		return blockedList.get(playerObjId);
	}

	public boolean contains(int playerObjectId) {
		return blockedList.containsKey(playerObjectId);
	}

	/**
	 * Returns the number of blocked players in this list
	 * 
	 * @return blockedList.size()
	 */
	public int getSize() {
		return blockedList.size();
	}

	public boolean isFull() {
		return getSize() >= MAX_BLOCKS;
	}

	@Override
	public Iterator<BlockedPlayer> iterator() {
		return blockedList.values().iterator();
	}

}
