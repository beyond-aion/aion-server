package com.aionemu.gameserver.taskmanager.fromdb.handler;

/**
 * @author nrg
 */
public enum TaskFromDBHandlerHolder {

	SHUTDOWN(ShutdownHandler.class),
	RESTART(RestartHandler.class);

	private Class<? extends TaskFromDBHandler> taskClass;

	private TaskFromDBHandlerHolder(Class<? extends TaskFromDBHandler> taskClass) {
		this.taskClass = taskClass;
	}

	public Class<? extends TaskFromDBHandler> getTaskClass() {
		return taskClass;
	}
}
