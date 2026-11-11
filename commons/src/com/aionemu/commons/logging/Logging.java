package com.aionemu.commons.logging;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import com.aionemu.commons.Banner;

import ch.qos.logback.classic.ClassicConstants;

public class Logging {

	private Logging() {
	}

	/**
	 * This method must be called before instantiating any logger
	 */
	public static void init() {
		System.setProperty(ClassicConstants.CONFIG_FILE_PROPERTY, "config/logback.xml");
		LogLevelHighlightingCompositeConverter.register();
		RealGrayCompositeConverter.register();
		Banner.print();
		archiveLogs();
	}

	public static String formatStartupMessage(String server, long startTimeNanos) {
		DecimalFormat df = new DecimalFormat("0.0##", DecimalFormatSymbols.getInstance(Locale.US));
		float startTime = Duration.ofNanos(System.nanoTime() - startTimeNanos).toMillis() / 1000f;
		float processUptime = ManagementFactory.getRuntimeMXBean().getUptime() / 1000f;
		return "%s started in %s seconds (process running for %s)".formatted(server, df.format(startTime), df.format(processUptime));
	}

	private static void archiveLogs() {
		try {
			Path logFolder = Path.of("log");
			Path oldLogsFolder = logFolder.resolve("archived");
			Path startTimeFile = logFolder.resolve("[server_start_marker]");
			Instant lastStartTime = Files.exists(startTimeFile) ? Files.getLastModifiedTime(startTimeFile).toInstant() : null;
			AtomicReference<FileTime> lastStopTime = new AtomicReference<>();
			DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH.mm").withZone(ZoneId.systemDefault());
			Path stagingFolder = oldLogsFolder.resolve(dtf.format(lastStartTime == null ? Instant.now() : lastStartTime));

			List<Path> logFiles = moveOldLogFiles(logFolder, oldLogsFolder, stagingFolder, lastStopTime);
			// update the start time after old log files have been moved successfully, so that exceptions don't alter the time for the next archival attempt
			updateStartTime(startTimeFile);

			if (!logFiles.isEmpty()) {
				String outFilename = (lastStartTime == null ? "Unknown" : dtf.format(lastStartTime)) + " to " + dtf.format(lastStopTime.get().toInstant()) + ".zip";
				// platform thread so that the JVM completes this task before shutting down
				Thread.ofPlatform().name("Log-Archiver").start(() -> {
					try {
						createArchive(oldLogsFolder.resolve(outFilename), logFiles, stagingFolder);
						delete(logFiles);
					} catch (IOException e) {
						throw new RuntimeException("Error archiving old logs", e);
					}
				});
			}
		} catch (IOException | SecurityException e) {
			throw new RuntimeException("Error gathering old logs", e);
		}
	}

	private static List<Path> moveOldLogFiles(Path logFolder, Path oldLogsFolder, Path stagingFolder, AtomicReference<FileTime> lastStopTime) throws IOException {
		if (!Files.exists(logFolder))
			return Collections.emptyList();
		List<Path> logFiles = new ArrayList<>();
		Files.walkFileTree(logFolder, new SimpleFileVisitor<>() {

			@Override
			public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
				// Skip the archived/ folder to avoid re-processing leftover subdirectories
				if (dir.startsWith(oldLogsFolder))
					return FileVisitResult.SKIP_SUBTREE;
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
				if (attrs.isRegularFile() && file.toString().toLowerCase().endsWith(".log")) {
					Path target = stagingFolder.resolve(logFolder.relativize(file));
					Files.createDirectories(target.getParent());
					logFiles.add(Files.move(file, target, StandardCopyOption.REPLACE_EXISTING));
					lastStopTime.updateAndGet(t -> t == null || t.compareTo(attrs.lastModifiedTime()) < 0 ? attrs.lastModifiedTime() : t);
				}
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
				if (exc != null)
					throw exc;
				try {
					if (!dir.equals(logFolder))
						Files.delete(dir);
				} catch (DirectoryNotEmptyException _) {
				}
				return FileVisitResult.CONTINUE;
			}
		});
		return logFiles;
	}

	private static void updateStartTime(Path startTimeFile) throws IOException {
		if (!Files.exists(startTimeFile)) {
			Files.createDirectories(startTimeFile.getParent());
			Files.createFile(startTimeFile);
		}
		Files.setLastModifiedTime(startTimeFile, FileTime.fromMillis(ManagementFactory.getRuntimeMXBean().getStartTime()));
	}

	private static void createArchive(Path file, List<Path> files, Path rootDirectory) throws IOException {
		Files.createDirectories(file.getParent());
		try (ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(Files.newOutputStream(file)))) {
			out.setMethod(ZipOutputStream.DEFLATED);
			out.setLevel(Deflater.BEST_COMPRESSION);
			for (Path logFile : files) {
				ZipEntry zipEntry = new ZipEntry(rootDirectory.relativize(logFile).toString());
				zipEntry.setLastModifiedTime(Files.getLastModifiedTime(logFile));
				out.putNextEntry(zipEntry);
				Files.copy(logFile, out);
			}
		}
	}

	private static void delete(List<Path> files) throws IOException {
		TreeSet<Path> parents = new TreeSet<>(Comparator.reverseOrder());
		for (Path logFile : files) {
			Files.deleteIfExists(logFile);
			parents.add(logFile.getParent());
		}
		for (Path parent : parents) {
			try {
				Files.deleteIfExists(parent);
			} catch (DirectoryNotEmptyException _) {
			}
		}
	}
}
