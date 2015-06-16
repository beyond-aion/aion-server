package com.aionemu.gameserver.dao;

import java.util.ArrayList;

import com.aionemu.commons.database.dao.DAO;
import com.aionemu.gameserver.taskmanager.fromdb.trigger.TaskFromDBTrigger;

/**
 * @author Divinity, nrg
 */
public abstract class TaskFromDBDAO implements DAO {

	/**
	 * Return all tasks from DB
	 * 
	 * @return all tasks
	 */
	public abstract ArrayList<TaskFromDBTrigger> getAllTasks();

	/**
	 * Returns class name that will be uses as unique identifier for all DAO classes
	 * 
	 * @return class name
	 */
	@Override
	public final String getClassName() {
		return TaskFromDBDAO.class.getName();
	}
}
