package com.aionemu.chatserver;

import org.slf4j.LoggerFactory;

import com.aionemu.chatserver.network.netty.NettyServer;
import com.aionemu.commons.utils.ExitCode;

import ch.qos.logback.classic.LoggerContext;

/**
 * @author nrg
 */
public class ShutdownHook extends Thread {

	private static final ShutdownHook instance = new ShutdownHook();

	private boolean restartOnly = false;

	public static ShutdownHook getInstance() {
		return instance;
	}

	public void setRestartOnly(boolean restartOnly) {
		this.restartOnly = restartOnly;
	}

	@Override
	public void run() {
		NettyServer.getInstance().shutdownAll();

		// shut down logger factory to flush all pending log messages
		((LoggerContext) LoggerFactory.getILoggerFactory()).stop();

		// Do system exit
		Runtime.getRuntime().halt(restartOnly ? ExitCode.RESTART : ExitCode.NORMAL);
	}
}
