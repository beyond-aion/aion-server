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
 * @author -Nemesiss-, Neon
 */
public class PlayerContainer implements Iterable<Player> {

	private final ConcurrentHashMap<Integer, Player> playersById = new ConcurrentHashMap<>();
	private final ConcurrentHashMap<String, Player> playersByName = new ConcurrentHashMap<>();

	public void add(Player player) {
		if (playersById.put(player.getObjectId(), player) != null)
			throw new DuplicateAionObjectException(player, playersById.get(player.getObjectId()));
		if (playersByName.put(player.getName(), player) != null)
			throw new DuplicateAionObjectException(player, playersByName.get(player.getName()));
	}

	public void remove(Player player) {
		playersById.remove(player.getObjectId());
		playersByName.remove(player.getName());
	}

	public Player get(int objectId) {
		return playersById.get(objectId);
	}

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

	public void updateCachedPlayerName(String oldName, Player player) {
		playersByName.compute(oldName, (n, p)-> {
			playersByName.put(player.getName(), player);
			return player.equals(p) ? null : p;
		});
	}
}
