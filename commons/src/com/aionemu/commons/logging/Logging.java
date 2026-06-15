package com.aionemu.commons.logging;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import ch.qos.logback.classic.ClassicConstants;

public class Logging {

	/**
	 * This method must be called before instantiating any logger
	 */
	public static void init() {
		System.setProperty(ClassicConstants.CONFIG_FILE_PROPERTY, "config/logback.xml");
		archiveLogs();
	}

	private static void archiveLogs() {
		try {
			Path logFolder = Path.of("log");
			Path oldLogsFolder = logFolder.resolve("archived");
			Path startTimeFile = logFolder.resolve("[server_start_marker]");
			List<Path> logFiles = new ArrayList<>();
			Instant lastStartTime = null;
			AtomicReference<FileTime> lastStopTime = new AtomicReference<>();

			Files.createDirectories(startTimeFile.getParent());
			if (Files.exists(startTimeFile))
				lastStartTime = Files.getLastModifiedTime(startTimeFile).toInstant();
			else
				Files.createFile(startTimeFile);
			Files.setLastModifiedTime(startTimeFile, FileTime.fromMillis(ManagementFactory.getRuntimeMXBean().getStartTime())); // update with new start time

			Files.walkFileTree(logFolder, new SimpleFileVisitor<>() {

				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
					if (attrs.isRegularFile() && file.toString().toLowerCase().endsWith(".log")) {
						logFiles.add(file);
						lastStopTime.updateAndGet(t -> t == null || t.compareTo(attrs.lastModifiedTime()) < 0 ? attrs.lastModifiedTime() : t);
					}
					return FileVisitResult.CONTINUE;
				}
			});

			if (!logFiles.isEmpty()) {
				DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH.mm").withZone(ZoneId.systemDefault());
				String outFilename = (lastStartTime == null ? "Unknown" : dtf.format(lastStartTime)) + " to " + dtf.format(lastStopTime.get().toInstant()) + ".zip";
				createArchive(oldLogsFolder.resolve(outFilename), logFiles, logFolder);
				delete(logFiles);
			}
		} catch (IOException | SecurityException e) {
			throw new RuntimeException("Error gathering and archiving old logs", e);
		}
	}

	private static void createArchive(Path file, List<Path> files, Path rootDirectory) throws IOException {
		Files.createDirectories(file.getParent());
		try (ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(Files.newOutputStream(file)))) {
			out.setMethod(ZipOutputStream.DEFLATED);
			out.setLevel(Deflater.BEST_COMPRESSION);
			for (Path logFile : files) {
				out.putNextEntry(new ZipEntry(rootDirectory.relativize(logFile).toString()));
				Files.copy(logFile, out);
			}
		}
	}

	private static void delete(List<Path> files) throws IOException {
		for (Path logFile : files)
			Files.delete(logFile);
		// attempt to delete parent folders (only deletes empty folders)
		files.stream().map(Path::getParent).distinct().forEach(parent -> parent.toFile().delete());
	}
}
