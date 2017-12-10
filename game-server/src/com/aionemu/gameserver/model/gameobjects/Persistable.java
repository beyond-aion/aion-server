package com.aionemu.gameserver.model.gameobjects;

import java.util.function.Predicate;

/**
 * @author Neon
 */
public interface Persistable {

	Predicate<Persistable> NEW = newPredicate(PersistentState.NEW);
	Predicate<Persistable> CHANGED = newPredicate(PersistentState.UPDATE_REQUIRED);
	Predicate<Persistable> DELETED = newPredicate(PersistentState.DELETED);

	PersistentState getPersistentState();

	void setPersistentState(PersistentState state);

	/**
	 * @return Predicate that matches all Persistables with the given state.
	 */
	static Predicate<Persistable> newPredicate(PersistentState state) {
		return persistable -> persistable != null && persistable.getPersistentState() == state;
	}

	@SuppressWarnings("hiding") // silence eclipse false warning
	enum PersistentState {
		NEW,
		UPDATE_REQUIRED,
		UPDATED,
		DELETED,
		NOACTION
	}
}
