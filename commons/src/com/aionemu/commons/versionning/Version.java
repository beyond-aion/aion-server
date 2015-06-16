package com.aionemu.commons.versionning;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import org.slf4j.LoggerFactory;

import org.slf4j.Logger;

/**
 * @author lord_rex
 */
public class Version {

	private static final Logger log = LoggerFactory.getLogger(Version.class);
	private String revision;
	private String date;
	private String branch;
	private String commitTime;

	public Version() {
	}

	public Version(Class<?> c) {
		loadInformation(c);
	}

	public void loadInformation(Class<?> c) {
		File jarName = null;
		try {
			jarName = Locator.getClassSource(c);
			try (JarFile jarFile = new JarFile(jarName)) {
				Attributes attrs = jarFile.getManifest().getMainAttributes();
				this.revision = getAttribute("Revision", attrs);
				this.date = getAttribute("Date", attrs);
				this.branch = getAttribute("Branch", attrs);
				this.commitTime = getAttribute("CommitTime", attrs);
			}
		}
		catch (IOException e) {
			log.error("Unable to get Soft information\nFile name '" + (jarName == null ? "null" : jarName.getAbsolutePath())
				+ "' isn't a valid jar", e);
		}

	}

	public void transferInfo(String jarName, String type, File fileToWrite) {
		try {
			if (!fileToWrite.exists()) {
				log.error("Unable to Find File :" + fileToWrite.getName() + " Please Update your " + type);
				return;
			}
			// Open the JAR file
			try (JarFile jarFile = new JarFile("./" + jarName)) {
				// Get the manifest
				Manifest manifest = jarFile.getManifest();
				// Write the manifest to a file
				OutputStream fos = new FileOutputStream(fileToWrite);
				manifest.write(fos);
				fos.close();
			}
		}
		catch (IOException e) {
			log.error("Error, " + e);
		}
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

	public final String getCommitTime() {
		return commitTime;
	}

	private final String getAttribute(String attribute, Attributes attrs) {
		String date = attrs.getValue(attribute);
		return date != null ? date : "Unknown " + attribute;
	}
}
