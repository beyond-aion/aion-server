package com.aionemu.gameserver.utils.idfactory;

import java.util.BitSet;
import java.util.Collection;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.database.dao.DAOManager;
import com.aionemu.commons.utils.GenericValidator;
import com.aionemu.gameserver.dao.GuideDAO;
import com.aionemu.gameserver.dao.HousesDAO;
import com.aionemu.gameserver.dao.InventoryDAO;
import com.aionemu.gameserver.dao.LegionDAO;
import com.aionemu.gameserver.dao.MailDAO;
import com.aionemu.gameserver.dao.PlayerDAO;
import com.aionemu.gameserver.dao.PlayerPetsDAO;
import com.aionemu.gameserver.dao.PlayerRegisteredItemsDAO;
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
	private final BitSet idList;

	/**
	 * Synchronization of bitset
	 */
	private final ReentrantLock lock;

	/**
	 * Id that will be used as minimal on next id request
	 */
	private volatile int nextMinId = 1;

	private IDFactory() {
		idList = new BitSet();
		lock = new ReentrantLock();
		lockIds(0);
		// TODO - I can refactor later
		PlayerDAO playerDAO = new PlayerDAO();
		InventoryDAO inventoryDAO = new InventoryDAO();
		PlayerRegisteredItemsDAO playerRegisteredItemsDAO = new PlayerRegisteredItemsDAO();
		LegionDAO legionDAO = new LegionDAO();
		MailDAO mailDAO = new MailDAO();
		GuideDAO guideDAO = new GuideDAO();
		HousesDAO housesDAO = new HousesDAO();
		PlayerPetsDAO playerPetsDAO = new PlayerPetsDAO();

		// Here should be calls to all IDFactoryAwareDAO implementations to initialize already used IDs
		lockIds(playerDAO.getUsedIDs());
		lockIds(inventoryDAO.getUsedIDs());
		lockIds(playerRegisteredItemsDAO.getUsedIDs());
		lockIds(legionDAO.getUsedIDs());
		lockIds(mailDAO.getUsedIDs());
		lockIds(guideDAO.getUsedIDs());
		lockIds(housesDAO.getUsedIDs());
		lockIds(playerPetsDAO.getUsedIDs());

		log.info("IDFactory: " + getUsedCount() + " IDs used.");
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
		try {
			lock.lock();

			int id = nextValidId(nextMinId);
			idList.set(id);

			nextMinId = id + 1;
			return id;
		} finally {
			lock.unlock();
		}
	}

	private int nextValidId(int searchIndex) {
		int id = idList.nextClearBit(searchIndex);
		if (id == Integer.MIN_VALUE) // Integer.MIN_VALUE means we have used up all available IDs
			throw new IDFactoryError("All IDs are used, please clear your database");
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
			boolean status = idList.get(id);
			if (status) {
				throw new IDFactoryError("ID " + id + " is already taken, fatal error!!!");
			}
			idList.set(id);
		}
	}

	/**
	 * Releases given id
	 *
	 * @param id
	 *          id to release
	 */
	public void releaseId(int id) {
		try {
			lock.lock();
			if (!release(id))
				log.warn("Couldn't release ID " + id + " because it wasn't taken", new IllegalArgumentException()); // print stack
		} finally {
			lock.unlock();
		}
	}

	/**
	 * Internal helper method without synchronization
	 *
	 * @return True if the ID could be released, false if it was not taken
	 */
	private boolean release(int id) {
		boolean status = idList.get(id);
		if (!status)
			return false;
		idList.clear(id);
		if (id < nextMinId || nextMinId == Integer.MIN_VALUE)
			nextMinId = id;
		return true;
	}

	public void releaseObjectIds(Collection<? extends AionObject> unusedObjects) {
		if (GenericValidator.isBlankOrNull(unusedObjects))
			return;

		try {
			lock.lock();
			for (AionObject obj : unusedObjects) {
				if (!release(obj.getObjectId()))
					log.warn("Couldn't release object ID of " + obj + " (ID wasn't taken)", new IllegalArgumentException()); // print object and stack
			}
		} finally {
			lock.unlock();
		}
	}

	/**
	 * Returns amount of used ids
	 *
	 * @return amount of used ids
	 */
	public int getUsedCount() {
		try {
			lock.lock();
			return idList.cardinality();
		} finally {
			lock.unlock();
		}
	}

	private static class SingletonHolder {

		protected static final IDFactory instance = new IDFactory();
	}
}
