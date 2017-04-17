package com.aionemu.gameserver.world.container;

import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.world.exceptions.DuplicateAionObjectException;

/**
 * Container for storing Players by objectId and name.
 * 
 * @author -Nemesiss-
 * @modified Neon
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
			throw new DuplicateAionObjectException(player, playersById.get(player.getObjectId()));
		if (playersByName.put(player.getName(), player) != null)
			throw new DuplicateAionObjectException(player, playersByName.get(player.getName()));
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
	 * Gets the player object by his object id.
	 * 
	 * @param objectId
	 *          - players object id
	 * @return Player with given ojectId or null if player with given objectId is not logged on.
	 */
	public Player get(int objectId) {
		return playersById.get(objectId);
	}

	/**
	 * Gets the player object by his name.
	 * 
	 * @param name
	 *          - players name
	 * @return Player with given name or null if player with given name is not logged on / recently renamed himself.
	 */
	public Player get(String name) {
		return playersByName.get(name);
	}

	@Override
	public Iterator<Player> iterator() {
		return playersById.values().iterator();
	}

	public Collection<Player> getAllPlayers() {
		return playersById.values().stream().filter(p -> p != null).collect(Collectors.toList()); // ensure there are no null values (due to concurrent object removal)
	}
}
