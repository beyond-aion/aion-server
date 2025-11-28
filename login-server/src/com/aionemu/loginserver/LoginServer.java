package com.aionemu.loginserver;

import org.slf4j.LoggerFactory;

import com.aionemu.commons.database.DatabaseFactory;
import com.aionemu.commons.logging.Logging;
import com.aionemu.commons.utils.concurrent.UncaughtExceptionHandler;
import com.aionemu.commons.utils.info.SystemInfo;
import com.aionemu.commons.utils.info.VersionInfo;
import com.aionemu.loginserver.configs.Config;
import com.aionemu.loginserver.controller.BannedIpController;
import com.aionemu.loginserver.dao.BannedHddDAO;
import com.aionemu.loginserver.dao.BannedMacDAO;
import com.aionemu.loginserver.network.NetConnector;
import com.aionemu.loginserver.network.ncrypt.KeyGen;
import com.aionemu.loginserver.service.PlayerTransferService;

import ch.qos.logback.classic.LoggerContext;

/**
 * @author -Nemesiss-
 */
class LoginServer {

	void main() {
		Logging.init(); // must run before instantiating any logger
		Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler());

		Config.load();
		DatabaseFactory.init();
		KeyGen.init();

		GameServerTable.load();
		BannedIpController.start();
		BannedMacDAO.cleanExpiredBans();
		BannedHddDAO.cleanExpiredBans();

		PlayerTransferService.getInstance();

		VersionInfo.logAll(LoginServer.class);
		SystemInfo.logAll();

		NetConnector.connect();
		Runtime.getRuntime().addShutdownHook(new ShutdownHook());
	}

	private static class ShutdownHook extends Thread {

		@Override
		public void run() {
			PlayerTransferService.getInstance().shutdown();
			NetConnector.shutdown();
			// shut down logger factory to flush all pending log messages
			((LoggerContext) LoggerFactory.getILoggerFactory()).stop();
		}
	}
}
