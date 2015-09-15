package com.aionemu.loginserver.taskmanager;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.database.dao.DAOManager;
import com.aionemu.loginserver.dao.TaskFromDBDAO;
import com.aionemu.loginserver.taskmanager.trigger.TaskFromDBTrigger;

/**
 * @author nrg
 */
public class TaskFromDBManager {

	private static final Logger log = LoggerFactory.getLogger(TaskFromDBManager.class);
	private List<TaskFromDBTrigger> tasksList;

	private TaskFromDBManager() {
		tasksList = getDAO().getAllTasks();
		log.info("Loaded " + tasksList.size() + " task" + (tasksList.size() > 1 ? "s" : "") + " from the database");

		registerTaskInstances();
	}

	/**
	 * Launching & checking task process
	 */
	private void registerTaskInstances() {
		// For all tasks from DB
		for (TaskFromDBTrigger trigger : tasksList) {
			if (trigger.isValid()) {
				trigger.initTrigger();
			} else {
				log.error("Invalid task from db with ID: " + trigger.getTaskId());
			}
		}
	}

	/**
	 * Retuns {@link com.aionemu.gameserver.dao.TaskFromDBDAO} , just a shortcut
	 *
	 * @return {@link com.aionemu.gameserver.dao.TaskFromDBDAO}
	 */
	private static TaskFromDBDAO getDAO() {
		return DAOManager.getDAO(TaskFromDBDAO.class);
	}

	/**
	 * Get the instance
	 *
	 * @return
	 */
	public static TaskFromDBManager getInstance() {
		return TaskFromDBManager.SingletonHolder.instance;
	}

	/**
	 * SingletonHolder
	 */
	private static class SingletonHolder {

		protected static final TaskFromDBManager instance = new TaskFromDBManager();
	}
}
