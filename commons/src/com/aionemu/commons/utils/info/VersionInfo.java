package com.aionemu.commons.utils.info;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.jar.Attributes;
import java.util.jar.JarFile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author lord_rex, Neon
 */
public class VersionInfo {

	private static final Logger log = LoggerFactory.getLogger(VersionInfo.class);

	private String source;
	private String revision;
	private String date;
	private String branch;

	/**
	 * Constructs a VersionInfo object holding the version information of the specified classes source (JAR)
	 */
	protected VersionInfo(Class<?> c) {
		try {
			File sourceFile = new File(c.getProtectionDomain().getCodeSource().getLocation().toURI());
			if (!sourceFile.exists())
				throw new FileNotFoundException();

			if (sourceFile.isDirectory()) { // e.g. when class is run from IDE
				source = sourceFile.getPath(); // classes folder
				File classSource = new File(c.getClassLoader().getResource(c.getCanonicalName().replace('.', '/') + ".class").toURI());
				date = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").format(classSource.lastModified()); // build date = last modified of the .class file
				return;
			}
			source = sourceFile.getName();
			try (JarFile jarFile = new JarFile(sourceFile)) {
				Attributes attrs = jarFile.getManifest().getMainAttributes();
				revision = attrs.getValue("Revision");
				date = attrs.getValue("Date");
				branch = attrs.getValue("Branch");
			}
		} catch (IOException | URISyntaxException e) {
			log.error("Could not get manifest information", e);
		}
	}

	public final String getSourceName() {
		return source;
	}

	public final String getRevision() {
		return revision;
	}

	public final String getDate() {
		return date;
	}

	public final String getBranch() {
		return branch;
	}

	public String[] getAllInfo() {
		return new String[] { "[" + source + "]", " Revision:\t" + revision, " Branch:\t" + branch, " Build Date:\t" + date };
	}
}
