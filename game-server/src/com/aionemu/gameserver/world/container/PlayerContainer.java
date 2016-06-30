package com.aionemu.gameserver.world.container;

import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.world.exceptions.DuplicateAionObjectException;

/**
 * Container for storing Players by objectId and name.
 * 
 * @author -Nemesiss-
 */
public class PlayerContainer implements Iterable<Player> {

	/**
	 * Map<ObjectId,Player>
	 */
	private final ConcurrentHashMap<Integer, Player> playersById = new ConcurrentHashMap<>();
	/**
	 * Map<Name,Player>
	 */
	private final ConcurrentHashMap<String, Player> playersByName = new ConcurrentHashMap<>();

	/**
	 * Add Player to this Container.
	 * 
	 * @param player
	 */
	public void add(Player player) {
		if (playersById.put(player.getObjectId(), player) != null)
			throw new DuplicateAionObjectException();
		if (playersByName.put(player.getName(), player) != null)
			throw new DuplicateAionObjectException();
	}

	/**
	 * Remove Player from this Container.
	 * 
	 * @param player
	 */
	public void remove(Player player) {
		playersById.remove(player.getObjectId());
		playersByName.remove(player.getName());
	}

	/**
	 * Get Player object by objectId.
	 * 
	 * @param objectId
	 *          - ObjectId of player.
	 * @return Player with given ojectId or null if Player with given objectId is not logged.
	 */
	public Player get(int objectId) {
		return playersById.get(objectId);
	}

	/**
	 * Get Player object by name.
	 * 
	 * @param name
	 *          - name of player
	 * @return Player with given name or null if Player with given name is not logged.
	 */
	public Player get(String name) {
		return playersByName.get(name);
	}

	@Override
	public Iterator<Player> iterator() {
		return playersById.values().iterator();
	}

	public Collection<Player> getAllPlayers() {
		return playersById.values();
	}
}
