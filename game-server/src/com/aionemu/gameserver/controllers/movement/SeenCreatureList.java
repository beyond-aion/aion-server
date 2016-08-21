package com.aionemu.gameserver.controllers.movement;

import com.aionemu.gameserver.model.gameobjects.Creature;

import javolution.util.FastMap;

/**
 * @author Rolandas
 */
public class SeenCreatureList {

	private FastMap<Integer, Creature> seenCreatures;

	public boolean add(Creature creature) {
		if (seenCreatures == null)
			seenCreatures = new FastMap<>();
		return seenCreatures.putIfAbsent(creature.getObjectId(), creature) == null;
	}

	public boolean remove(Creature creature) {
		if (seenCreatures == null)
			return false;
		return seenCreatures.remove(creature.getObjectId()) != null;
	}

	public void clear() {
		if (seenCreatures != null)
			seenCreatures.clear();
	}

	public boolean contains(Creature creature) {
		if (seenCreatures == null)
			return false;
		return seenCreatures.containsKey(creature.getObjectId());
	}
}
