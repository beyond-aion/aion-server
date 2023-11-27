package com.aionemu.commons.utils.info;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicReference;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.utils.ClassUtils;

/**
 * @author lord_rex, Neon
 */
public class VersionInfo {

	private static final Logger log = LoggerFactory.getLogger(VersionInfo.class);
	public static final VersionInfo commons = new VersionInfo(VersionInfo.class);

	private String source;
	private String revision;
	private String branch;
	private Instant buildDate;
	private int classFileVersion;

	/**
	 * Constructs a VersionInfo object holding the version information of the specified classes JAR or directory
	 */
	public VersionInfo(Class<?> c) {
		try {
			File sourceFile = new File(c.getProtectionDomain().getCodeSource().getLocation().toURI());
			if (sourceFile.isDirectory()) { // when application is run from IDE
				source = Paths.get("").toAbsolutePath().relativize(sourceFile.toPath()).toString();
				File latestFile = findLatestFile(sourceFile);
				buildDate = Instant.ofEpochMilli(latestFile.lastModified());
				classFileVersion = ClassUtils.readClassFileVersion(new FileInputStream(latestFile), latestFile.getPath());
			} else {
				source = sourceFile.getName();
				try (JarFile jarFile = new JarFile(sourceFile)) {
					Attributes attrs = jarFile.getManifest().getMainAttributes();
					revision = attrs.getValue("Revision");
					branch = attrs.getValue("Branch");
					buildDate = Instant.parse(attrs.getValue("Date"));
					JarEntry jarEntry = jarFile.stream().filter(e -> !e.isDirectory() && e.getName().endsWith(".class")).findFirst().get();
					classFileVersion = ClassUtils.readClassFileVersion(jarFile.getInputStream(jarEntry), jarEntry.getName());
				}
			}
		} catch (Exception e) {
			log.error("Could not get version information", e);
		}
	}

	private File findLatestFile(File sourceFile) throws IOException {
		AtomicReference<FileTime> latestChange = new AtomicReference<>();
		return Files.find(sourceFile.toPath(), Integer.MAX_VALUE, (filePath, fileAttr) -> {
			FileTime lastModified = fileAttr.lastModifiedTime();
			return fileAttr.isRegularFile() && filePath.toString().endsWith(".class")
				&& latestChange.updateAndGet(t -> t == null || lastModified.compareTo(t) > 0 ? lastModified : t) == lastModified;
		})
			.reduce((first, second) -> second) // due to the file matcher the last element will be the latest, so no need to call .max()
			.get()
			.toFile();
	}

	public String getSource() {
		return source;
	}

	public String getRevision() {
		return revision;
	}

	public String getBranch() {
		return branch;
	}

	public Instant getBuildDate() {
		return buildDate;
	}

	public int getClassFileVersion() {
		return classFileVersion;
	}

	public int getClassFileJavaVersion() {
		// assuming each new major Java version increases the class file version by 1, we can calculate the target Java version of the class file
		return Runtime.version().feature() - (int) Float.parseFloat(System.getProperty("java.class.version")) + classFileVersion;
	}

	public String getBuildInfo(ZoneId timeZoneId) {
		DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").withZone(timeZoneId);
		String rev = revision == null ? null : revision + (branch == null ? "" : " (" + branch + ')');
		String buildDateAndJavaVersion = "built on " + dateTimeFormatter.format(buildDate) + " for Java " + getClassFileJavaVersion();
		return (rev == null ? "" : "revision " + rev + " ") + buildDateAndJavaVersion;
	}

	@Override
	public String toString() {
		return toString(ZoneId.systemDefault());
	}

	public String toString(ZoneId timeZoneId) {
		return toString(timeZoneId, 0);
	}

	private String toString(ZoneId timeZoneId, int sourceLeftPadToWidth) {
		String sourceLeftPadded = sourceLeftPadToWidth > source.length() ? String.format("%" + sourceLeftPadToWidth + "s", source) : source;
		return sourceLeftPadded + " " + getBuildInfo(timeZoneId);
	}

	public static void logAll(Class<?> c) {
		logAll(new VersionInfo(c), ZoneId.systemDefault());
	}

	public static void logAll(VersionInfo versionInfo, ZoneId timeZoneId) {
		int maxSourceLength = Math.max(VersionInfo.commons.source.length(), versionInfo.source.length());
		log.info(VersionInfo.commons.toString(timeZoneId, maxSourceLength));
		log.info(versionInfo.toString(timeZoneId, maxSourceLength));
	}
}
