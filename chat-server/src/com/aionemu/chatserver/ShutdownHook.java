package com.aionemu.chatserver;

import com.aionemu.chatserver.network.netty.NettyServer;
import com.aionemu.chatserver.service.GameServerService;
import com.aionemu.commons.utils.ExitCode;

/**
 * @author nrg
 */
public class ShutdownHook extends Thread {

	private static final ShutdownHook instance = new ShutdownHook();

	/**
	 * Indicates wether the loginserver should shut dpwn or only restart
	 */
	private static boolean restartOnly = false;

	/**
	 * get the shutdown-hook instance the shutdown-hook instance is created by the first call of this function, but it has to be registrered externaly.
	 * 
	 * @return instance of Shutdown, to be used as shutdown hook
	 */
	public static ShutdownHook getInstance() {
		return instance;
	}

	/**
	 * Set's restartOnly attribute
	 * 
	 * @param restartOnly
	 *          Indicates wether the loginserver should shut dpwn or only restart
	 */
	public static void setRestartOnly(boolean restartOnly) {
		ShutdownHook.restartOnly = restartOnly;
	}

	@Override
	public void run() {
		NettyServer.getInstance().shutdownAll();
		GameServerService.getInstance().setOffline();

		// Do system exit
		if (restartOnly)
			Runtime.getRuntime().halt(ExitCode.CODE_RESTART);
		else
			Runtime.getRuntime().halt(ExitCode.CODE_NORMAL);
	}
}
