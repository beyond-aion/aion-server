package com.aionemu.chatserver;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.slf4j.LoggerFactory;

import com.aionemu.chatserver.configs.Config;
import com.aionemu.chatserver.network.netty.NettyServer;
import com.aionemu.chatserver.service.BroadcastService;
import com.aionemu.chatserver.service.ChatService;
import com.aionemu.chatserver.service.GameServerService;
import com.aionemu.chatserver.utils.IdFactory;
import com.aionemu.commons.database.DatabaseFactory;
import com.aionemu.commons.utils.concurrent.UncaughtExceptionHandler;
import com.aionemu.commons.utils.info.SystemInfo;
import com.aionemu.commons.utils.info.VersionInfo;

import ch.qos.logback.classic.ClassicConstants;
import ch.qos.logback.classic.LoggerContext;

/**
 * @author ATracer, KID, nrg
 */
public class ChatServer {

	private ChatServer() {
	}

	private static void archiveLogs() {
		try {
			Path logFolder = Paths.get("./log");
			Path oldLogsFolder = Paths.get(logFolder + "/archived");
			List<File> files = new ArrayList<>();
			File serverStartTimeFile = new File("./log/[server_start_marker]");
			long serverStartTime;
			long[] serverEndTime = { 0 }; // for mutability within a stream (file walker), we need to use an array here

			Files.createDirectories(serverStartTimeFile.toPath().getParent());
			serverStartTimeFile.createNewFile(); // creates the file only if it does not exists
			serverStartTime = serverStartTimeFile.lastModified();
			serverStartTimeFile.setLastModified(ManagementFactory.getRuntimeMXBean().getStartTime()); // update with new server start time

			Files.createDirectories(logFolder);
			Files.walkFileTree(logFolder, new SimpleFileVisitor<>() {

				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
					if (!attrs.isDirectory() && file.toString().toLowerCase().endsWith(".log")) {
						files.add(file.toFile());
						if (serverEndTime[0] < attrs.lastModifiedTime().toMillis())
							serverEndTime[0] = attrs.lastModifiedTime().toMillis();
					}
					return FileVisitResult.CONTINUE;
				}
			});

			if (!files.isEmpty()) {
				Files.createDirectories(oldLogsFolder);
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH.mm");
				String outFilename = (serverStartTime < serverEndTime[0] ? sdf.format(serverStartTime) : "Unknown") + " to " + sdf.format(serverEndTime[0])
					+ ".zip";
				try (ZipOutputStream out = new ZipOutputStream(new FileOutputStream(oldLogsFolder + "/" + outFilename))) {
					out.setMethod(ZipOutputStream.DEFLATED);
					out.setLevel(Deflater.BEST_COMPRESSION);
					for (File logFile : files) {
						try (FileInputStream in = new FileInputStream(logFile)) {
							out.putNextEntry(new ZipEntry(logFolder.relativize(logFile.toPath()).toString()));
							in.transferTo(out);
							out.closeEntry();
						}
					}
				}
				for (File logFile : files) { // remove files after successful archiving
					logFile.delete();
					logFile.getParentFile().delete(); // attempt to delete the parent directory (only succeeds if empty)
				}
			}
		} catch (IOException | SecurityException e) {
			throw new RuntimeException("Error gathering and archiving old logs, shutting down...", e);
		}
	}

	public static void main(final String[] args) {
		System.setProperty(ClassicConstants.CONFIG_FILE_PROPERTY, "config/logback.xml"); // must be set before instantiating any logger
		archiveLogs(); // must also run before instantiating any logger
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
