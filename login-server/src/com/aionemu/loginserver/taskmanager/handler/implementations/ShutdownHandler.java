package com.aionemu.loginserver.taskmanager.handler.implementations;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.loginserver.Shutdown;
import com.aionemu.loginserver.taskmanager.handler.TaskFromDBHandler;

/**
 * @author Divinity, nrg
 */
public class ShutdownHandler extends TaskFromDBHandler {

	private static final Logger log = LoggerFactory.getLogger(ShutdownHandler.class);

	@Override
	public boolean isValid() {
		return true;

	}

	@Override
	public void trigger() {
		log.info("Task[" + taskId + "] launched : shutting down the server !");

		Shutdown shutdown = Shutdown.getInstance();
		shutdown.setRestartOnly(false);
		shutdown.start();
	}
}
