package com.aionemu.chatserver;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;

import com.aionemu.chatserver.configs.Config;
import com.aionemu.chatserver.network.netty.NettyServer;
import com.aionemu.chatserver.service.BroadcastService;
import com.aionemu.chatserver.service.ChatService;
import com.aionemu.chatserver.service.GameServerService;
import com.aionemu.chatserver.service.RestartService;
import com.aionemu.chatserver.utils.IdFactory;
import com.aionemu.commons.database.DatabaseFactory;
import com.aionemu.commons.database.dao.DAOManager;
import com.aionemu.commons.utils.AEInfos;

/**
 * @author ATracer, KID, nrg
 */
public class ChatServer {

	/**
	 * Logger for this class.
	 */
	private static final Logger log = LoggerFactory.getLogger(ChatServer.class);

	private static void initalizeLoggger() {
		new File("./log/backup/").mkdirs();
		File[] files = new File("log").listFiles(new FilenameFilter() {

			@Override
			public boolean accept(File dir, String name) {
				return name.endsWith(".log");
			}
		});

		if (files != null && files.length > 0) {
			byte[] buf = new byte[1024];
			try {
				String outFilename = "./log/backup/" + new SimpleDateFormat("yyyy-MM-dd HHmmss").format(new Date()) + ".zip";
				ZipOutputStream out = new ZipOutputStream(new FileOutputStream(outFilename));
				out.setMethod(ZipOutputStream.DEFLATED);
				out.setLevel(Deflater.BEST_COMPRESSION);
				for (File logFile : files) {
					FileInputStream in = new FileInputStream(logFile);
					out.putNextEntry(new ZipEntry(logFile.getName()));
					int len;
					while ((len = in.read(buf)) > 0) {
						out.write(buf, 0, len);
					}
					out.closeEntry();
					in.close();
					logFile.delete();
				}
				out.close();
			} catch (IOException e) {
			}
		}
		LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
		try {
			JoranConfigurator configurator = new JoranConfigurator();
			configurator.setContext(lc);
			lc.reset();
			configurator.doConfigure("config/slf4j-logback.xml");
		} catch (JoranException je) {
			throw new RuntimeException("Failed to configure loggers, shutting down...", je);
		}
	}

	/**
	 * @param args
	 */
	public static void main(final String[] args) {
		long start = System.currentTimeMillis();

		initalizeLoggger();
		log.info("\f" + new SimpleDateFormat("yyyy-MM-dd HH-mm-ss").format(new Date(System.currentTimeMillis())) + "\f");
		(new ServerCommandProcessor()).start(); // Launch the server command processor thread
		Config.load();
		DatabaseFactory.init();
		DAOManager.init();
		IdFactory.getInstance();
		GameServerService.getInstance();
		BroadcastService.getInstance();
		ChatService.getInstance();
		NettyServer.getInstance();
		RestartService.getInstance();
		Runtime.getRuntime().addShutdownHook(ShutdownHook.getInstance());

		AEInfos.printAllInfos();
		log.info("AL Chat Server started in " + (System.currentTimeMillis() - start) / 1000 + " seconds.");
	}
}
