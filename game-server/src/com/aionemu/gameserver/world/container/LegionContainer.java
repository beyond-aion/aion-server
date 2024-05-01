package com.aionemu.gameserver.world.container;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.aionemu.gameserver.model.team.legion.Legion;
import com.aionemu.gameserver.world.exceptions.DuplicateAionObjectException;

/**
 * Container for storing Legions by legionId and name.
 * 
 * @author Simple
 */
public class LegionContainer implements Iterable<Legion> {

	/**
	 * Map<LegionId, Legion>
	 */
	private final Map<Integer, Legion> legionsById = new ConcurrentHashMap<>();
	/**
	 * Map<LegionName, Legion>
	 */
	private final Map<String, Legion> legionsByName = new ConcurrentHashMap<>();

	/**
	 * Add Legion to this Container.
	 * 
	 * @param legion
	 */
	public void add(Legion legion) {
		if (legion == null || legion.getName() == null)
			return;

		if (legionsById.put(legion.getLegionId(), legion) != null)
			throw new DuplicateAionObjectException(legion, legionsById.get(legion.getLegionId()));
		if (legionsByName.put(legion.getName().toLowerCase(), legion) != null)
			throw new DuplicateAionObjectException(legion, legionsByName.get(legion.getName().toLowerCase()));
	}

	/**
	 * Remove Legion from this Container.
	 * 
	 * @param legion
	 */
	public void remove(Legion legion) {
		legionsById.remove(legion.getLegionId());
		legionsByName.remove(legion.getName().toLowerCase());
	}

	/**
	 * Get Legion object by objectId.
	 * 
	 * @param legionId
	 *          - legionId of legion.
	 * @return Legion with given ojectId or null if Legion with given legionId is not logged.
	 */
	public Legion get(int legionId) {
		return legionsById.get(legionId);
	}

	/**
	 * Get Legion object by name.
	 * 
	 * @param name
	 *          - name of legion
	 * @return Legion with given name or null if Legion with given name is not logged.
	 */
	public Legion get(String name) {
		return legionsByName.get(name.toLowerCase());
	}

	/**
	 * Returns true if legion is in cached by id
	 * 
	 * @param legionId
	 * @return true or false
	 */
	public boolean contains(int legionId) {
		return legionsById.containsKey(legionId);
	}

	/**
	 * Returns true if legion is in cached by name
	 * 
	 * @param name
	 * @return true or false
	 */
	public boolean contains(String name) {
		return legionsByName.containsKey(name.toLowerCase());
	}

	@Override
	public Iterator<Legion> iterator() {
		return legionsById.values().iterator();
	}

	public void clear() {
		legionsById.clear();
		legionsByName.clear();
	}

	public void updateCachedLegionName(String oldName, Legion legion) {
		legionsByName.compute(oldName, (n, legionMember) -> {
			legionsByName.put(legion.getName(), legion);
			return null;
		});
	}
}
