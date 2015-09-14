package com.aionemu.loginserver.taskmanager.handler.implementations;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.loginserver.Shutdown;
import com.aionemu.loginserver.taskmanager.handler.TaskFromDBHandler;

/**
 * @author Divinity, nrg
 */
public class RestartHandler extends TaskFromDBHandler {

	private static final Logger log = LoggerFactory.getLogger(RestartHandler.class);

	@Override
	public void trigger() {
		log.info("Task[" + taskId + "] launched : restarting the server !");

		Shutdown shutdown = Shutdown.getInstance();
		shutdown.setRestartOnly(true);
		shutdown.start();
	}

	@Override
	public boolean isValid() {
		return true;
	}
}
