package com.aionemu.gameserver.utils.idfactory;

import java.util.BitSet;
import java.util.Collection;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.utils.GenericValidator;
import com.aionemu.gameserver.dao.*;
import com.aionemu.gameserver.model.gameobjects.AionObject;

/**
 * This class is responsible for id generation for all Aion-Emu objects.<br>
 * This class is Thread-Safe.<br>
 * This class is designed to be very strict with id usage. Any illegal ID acquiring operation will throw {@link IDFactoryError}
 *
 * @author SoulKeeper, Neon
 */
public class IDFactory {

	private static final Logger log = LoggerFactory.getLogger(IDFactory.class);

	/**
	 * Npcs with objectIds that match this bit pattern are always invisible on client side, not sure why:
	 * 
	 * <pre>
	 * ??0??000000??00?0?11001010101??
	 * </pre>
	 * 
	 * Invalid ID examples:
	 * 
	 * <pre>
	 * 6484, 6485, 6486, 6487,
	 * 14676, 14677, 14678, 14679,
	 * 39252, 39253, 39254, 39255,
	 * 47444, 47445, 47446, 47447,
	 * 268628, 268629, 268630, 268631,
	 * ...
	 * 1812773204, 1812773205, 1812773206, 1812773207
	 * </pre>
	 * 
	 * We forbid them to ensure they'll never get assigned to an npc.
	 */
	private static final int INVALID_ID_BIT_MASK = 0b0010011111100110101111111111100;
	private static final int INVALID_ID_BITCHECK = 0b0000000000000000001100101010100; // 6484

	/**
	 * Bitset that is used for all id's.<br>
	 * We are allowing BitSet to grow over time, so in the end it can be as big as {@link Integer#MAX_VALUE}
	 */
	private final BitSet idList = new BitSet();

	/**
	 * Synchronization of bitset
	 */
	private final ReentrantLock lock = new ReentrantLock();

	/**
	 * Id that will be used as minimal on next id request
	 */
	private volatile int nextMinId = 1;

	private IDFactory() {
		lockIds(0);
		initializeUsedIds();
		log.info("IDFactory: {} IDs used.", getUsedCount());
	}

	public static IDFactory getInstance() {
		return SingletonHolder.instance;
	}

	/**
	 * Returns next free id.
	 *
	 * @return next free id
	 * @throws IDFactoryError
	 *           if there is no free id's
	 */
	public int nextId() {
		lock.lock();
		try {
			int id = nextValidId(nextMinId);
			idList.set(id);
			nextMinId = id + 1;
			return id;
		} finally {
			lock.unlock();
		}
	}

	public void releaseId(int id) {
		lock.lock();
		try {
			if (!release(id)) {
				log.warn("Couldn't release ID {} because it wasn't taken", id, new IllegalArgumentException());
			}
		} finally {
			lock.unlock();
		}
	}

	public void releaseObjectIds(Collection<? extends AionObject> unusedObjects) {
		if (GenericValidator.isBlankOrNull(unusedObjects))
			return;

		lock.lock();
		try {
			unusedObjects.forEach(obj -> {
				if (!release(obj.getObjectId())) {
					log.warn("Couldn't release object ID of {} (ID wasn't taken)", obj, new IllegalArgumentException());
				}
			});
		} finally {
			lock.unlock();
		}
	}

	/**
	 * @return amount of used ids
	 */
	public int getUsedCount() {
		lock.lock();
		try {
			return idList.cardinality();
		} finally {
			lock.unlock();
		}
	}

	private void initializeUsedIds() {
		lockIds(PlayerDAO.getUsedIDs());
		lockIds(InventoryDAO.getUsedIDs());
		lockIds(PlayerRegisteredItemsDAO.getUsedIDs());
		lockIds(LegionDAO.getUsedIDs());
		lockIds(MailDAO.getUsedIDs());
		lockIds(GuideDAO.getUsedIDs());
		lockIds(HousesDAO.getUsedIDs());
		lockIds(PlayerPetsDAO.getUsedIDs());
	}

	private int nextValidId(int searchIndex) {
		int id = idList.nextClearBit(searchIndex);
		if (id == Integer.MIN_VALUE) {
			throw new IDFactoryError("All IDs are used, please clear your database");
		}
		return isInvalidId(id) ? nextValidId(id + 1) : id;
	}

	private boolean isInvalidId(int id) {
		return (id & INVALID_ID_BIT_MASK) == INVALID_ID_BITCHECK;
	}

	/**
	 * Internal helper method without synchronization, to lock IDs during instantiation (public ID allocation should only happen via {@link #nextId()})
	 *
	 * @param ids
	 *          ids to lock
	 * @throws IDFactoryError
	 *           if some of the id's are already locked
	 */
	private void lockIds(int... ids) {
		for (int id : ids) {
			if (idList.get(id)) {
				throw new IDFactoryError("ID " + id + " is already taken, fatal error!!!");
			}
			idList.set(id);
		}
	}

	/**
	 * Internal helper method without synchronization
	 * 
	 * @return True if the ID could be released, false if it was not taken
	 */
	private boolean release(int id) {
		if (!idList.get(id))
			return false;
		idList.clear(id);
		if (id < nextMinId || nextMinId == Integer.MIN_VALUE)
			nextMinId = id;
		return true;
	}

	private static class SingletonHolder {

		private static final IDFactory instance = new IDFactory();
	}
}
