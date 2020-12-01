package com.aionemu.commons.database.dao;

import static com.aionemu.commons.database.DatabaseFactory.*;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.configs.DatabaseConfig;
import com.aionemu.commons.scripting.ScriptManager;
import com.aionemu.commons.scripting.classlistener.AggregatedClassListener;
import com.aionemu.commons.scripting.classlistener.OnClassLoadUnloadListener;
import com.aionemu.commons.scripting.classlistener.ScheduledTaskClassListener;

/**
 * This class manages {@link DAO} implementations, it resolves valid implementation for current database
 * 
 * @author SoulKeeper, Saelya
 */
public class DAOManager {

	/**
	 * Logger for DAOManager class
	 */
	private static final Logger log = LoggerFactory.getLogger(DAOManager.class);

	/**
	 * Collection of registered DAOs
	 */
	private static final Map<String, DAO> daoMap = new HashMap<>();

	/**
	 * This script manager is responsible for loading {@link com.aionemu.commons.database.dao.DAO} implementations
	 */
	private static ScriptManager scriptManager;

	/**
	 * Initializes DAOManager.
	 */
	public static void init() {
		try {
			scriptManager = new ScriptManager();

			// initialize default class listeners for this ScriptManager
			AggregatedClassListener acl = new AggregatedClassListener();
			acl.addClassListener(new OnClassLoadUnloadListener());
			acl.addClassListener(new ScheduledTaskClassListener());
			acl.addClassListener(new DAOLoader());
			scriptManager.setGlobalClassListener(acl);

			scriptManager.load(DatabaseConfig.DAO_DIRECTORY);
		} catch (RuntimeException e) {
			throw new Error(e.getMessage(), e);
		} catch (Exception e) {
			throw new Error("A fatal error occurred during loading or compiling the database handlers", e);
		}

		log.info("Loaded " + daoMap.size() + " DAO implementations.");
	}

	/**
	 * Shutdown DAOManager
	 */
	public static void shutdown() {
		scriptManager.shutdown();
		daoMap.clear();
		scriptManager = null;
	}

	/**
	 * Returns DAO implementation by DAO class. Typical usage:
	 * 
	 * <pre>
	 * 
	 * 
	 * AccountDAO dao = DAOManager.getDAO(AccountDAO.class);
	 * </pre>
	 * 
	 * @param clazz
	 *          Abstract DAO class implementation of which was registered
	 * @param <T>
	 *          Subclass of DAO
	 * @return DAO implementation
	 * @throws DAONotFoundException
	 *           if DAO implementation not found
	 */
	@SuppressWarnings("unchecked")
	public static <T extends DAO> T getDAO(Class<T> clazz) throws DAONotFoundException {

		DAO result = daoMap.get(clazz.getName());

		if (result == null) {
			String s = "DAO for class " + clazz.getSimpleName() + " not implemented";
			log.error(s);
			throw new DAONotFoundException(s);
		}

		return (T) result;
	}

	/**
	 * Registers {@link DAO}.<br>
	 * First it creates new instance of DAO, then invokes {@link DAO#supports(String, int, int)} <br>
	 * . If the result was positive - it associates DAO instance with {@link com.aionemu.commons.database.dao.DAO#getClassName()} <br>
	 * If another DAO was registered - {@link com.aionemu.commons.database.dao.DAOAlreadyRegisteredException} will be thrown
	 * 
	 * @param daoClass
	 *          DAO implementation
	 * @throws DAOAlreadyRegisteredException
	 *           if DAO is already registered
	 * @throws IllegalAccessException
	 *           if something went wrong during instantiation of DAO
	 * @throws InstantiationException
	 *           if something went wrong during instantiation of DAO
	 */
	public static void registerDAO(Class<? extends DAO> daoClass) throws DAOAlreadyRegisteredException, ReflectiveOperationException {
		DAO dao = daoClass.getDeclaredConstructor().newInstance();

		if (!dao.supports(getDatabaseName(), getDatabaseMajorVersion(), getDatabaseMinorVersion())) {
			return;
		}

		synchronized (DAOManager.class) {
			DAO oldDao = daoMap.get(dao.getClassName());
			if (oldDao != null) {
				throw new DAOAlreadyRegisteredException(
					"Couldn't register " + daoClass.getName() + ": className " + dao.getClassName() + " is used by " + oldDao.getClass().getName() + ".");
			}
			daoMap.put(dao.getClassName(), dao);
		}

		if (log.isDebugEnabled())
			log.debug("Registered DAO " + dao.getClassName());
	}

	/**
	 * Unregisters DAO class
	 * 
	 * @param daoClass
	 *          DAO implementation to unregister
	 */
	public static void unregisterDAO(Class<? extends DAO> daoClass) {
		synchronized (DAOManager.class) {
			for (DAO dao : daoMap.values()) {
				if (dao.getClass() == daoClass) {
					daoMap.remove(dao.getClassName());

					if (log.isDebugEnabled())
						log.debug("Unregistered DAO " + dao.getClassName());

					break;
				}
			}
		}
	}

	private DAOManager() {
	}
}
