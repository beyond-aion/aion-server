package com.aionemu.chatserver;

import org.slf4j.LoggerFactory;

import com.aionemu.chatserver.configs.Config;
import com.aionemu.chatserver.network.netty.NettyServer;
import com.aionemu.chatserver.service.BroadcastService;
import com.aionemu.chatserver.service.ChatService;
import com.aionemu.chatserver.service.GameServerService;
import com.aionemu.chatserver.utils.IdFactory;
import com.aionemu.commons.database.DatabaseFactory;
import com.aionemu.commons.logging.Logging;
import com.aionemu.commons.utils.concurrent.UncaughtExceptionHandler;
import com.aionemu.commons.utils.info.SystemInfo;
import com.aionemu.commons.utils.info.VersionInfo;

import ch.qos.logback.classic.LoggerContext;

/**
 * @author ATracer, KID, nrg
 */
class ChatServer {

	void main() {
		Logging.init(); // must run before instantiating any logger
		Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler());

		Config.load();
		DatabaseFactory.init();
		IdFactory.getInstance();
		GameServerService.getInstance();
		ChatService.getInstance();
		BroadcastService.getInstance();

		VersionInfo.logAll(ChatServer.class);
		SystemInfo.logAll();

		NettyServer.getInstance();
		Runtime.getRuntime().addShutdownHook(new ShutdownHook());
	}

	private static class ShutdownHook extends Thread {

		@Override
		public void run() {
			NettyServer.getInstance().shutdownAll();
			// shut down logger factory to flush all pending log messages
			((LoggerContext) LoggerFactory.getILoggerFactory()).stop();
		}
	}
}
