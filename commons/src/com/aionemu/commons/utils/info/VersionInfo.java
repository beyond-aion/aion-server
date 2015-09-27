package com.aionemu.commons.utils.info;

import java.io.File;
import java.io.IOException;
import java.util.jar.Attributes;
import java.util.jar.JarFile;

import org.apache.tools.ant.launch.Locator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author lord_rex
 * @modified Neon
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
		File sourceFile = Locator.getClassSource(c);
		this.source = sourceFile != null ? sourceFile.getName() : "";
		try (JarFile jarFile = new JarFile(sourceFile)) {
			Attributes attrs = jarFile.getManifest().getMainAttributes();
			this.revision = attrs.getValue("Revision");
			this.date = attrs.getValue("Date");
			this.branch = attrs.getValue("Branch");
		} catch (IOException e) {
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
		return new String[] {
			"[" + source + "]",
			" Revision:\t" + revision,
			" Branch:\t" + branch,
			" Build Date:\t" + date
		};
	}
}
