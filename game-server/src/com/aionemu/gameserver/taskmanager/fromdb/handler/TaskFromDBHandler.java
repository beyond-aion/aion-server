package com.aionemu.gameserver.taskmanager.fromdb.handler;

/**
 * @author nrg
 */
public abstract class TaskFromDBHandler {

	protected int taskId;
	protected String[] params = {""};

	/**
	 * Task's id
	 *
	 * @return
	 */
	public int getTaskId() {
		return taskId;
	}

	/**
	 * Task's id
	 *
	 * @param int
	 */
	public void setTaskId(int taskId) {
		this.taskId = taskId;
	}

	/**
	 * Task's execution paramaeters
	 *
	 * @return parameters
	 */
	public String[] getParams() {
		return params;
	}

	/**
	 * Task's execution param(s)
	 *
	 * @param params String[]
	 */
	public void setParams(String params[]) {
		this.params = params;
	}

	/**
	 * Check if the task's parameters are valid
	 *
	 * @return true if valid, false otherwise
	 */
	public abstract boolean isValid();

	/**
	 * Triggers the handlers functions
	 */
	public abstract void trigger();
}
