package com.aionemu.commons.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.Properties;

/**
 * This class is designed to simplify routine job with properties
 * 
 * @author SoulKeeper
 */
public class PropertiesUtils {

	/**
	 * @see #load(String, Properties)
	 */
	public static Properties load(String filename) throws IOException {
		return load(new File(filename), null);
	}

	/**
	 * @see #load(String, Properties)
	 */
	public static Properties load(String filename, Properties defaults) throws IOException {
		return load(new File(filename), defaults);
	}

	/**
	 * Loads properties from a file with the specified defaults as a backup.
	 * 
	 * @param file
	 *          File to load properties from
	 * @param defaults
	 *          Default values for the new properties, can be null
	 * @return Loaded properties (empty if file doesn't exist or is a directory)
	 */
	public static Properties load(File file, Properties defaults) throws IOException {
		Properties p = new Properties(defaults);
		if (file.isFile())
			loadProperties(p, file);
		return p;
	}

	private static void loadProperties(Properties properties, File file) throws IOException {
		try (FileInputStream fis = new FileInputStream(file)) {
			properties.load(fis);
		} catch (IOException e) {
			throw new IOException("Could not parse " + file, e);
		}
	}

	/**
	 * @see #loadFromDirectory(Properties, File, boolean)
	 */
	public static void loadFromDirectory(Properties properties, String dir, boolean recursive) throws IOException {
		loadFromDirectory(properties, new File(dir), recursive);
	}

	/**
	 * Loads all .properties files from a directory and fills the loaded values into given Properties object
	 *
	 * @param properties
	 *          Properties to fill
	 * @param dir
	 *          Directory where the .properties files are located
	 * @param recursive
	 *          Whether to parse subdirectories or not
	 * @throws IOException
	 *           If a file could not be read
	 */
	public static void loadFromDirectory(Properties properties, File dir, boolean recursive) throws IOException {
		for (Iterator<File> iter = propertiesFileIterator(dir, recursive); iter.hasNext();) {
			loadProperties(properties, iter.next());
		}
	}

	private static Iterator<File> propertiesFileIterator(File dir, boolean recursive) throws IOException {
		return Files.walk(dir.toPath(), recursive ? Integer.MAX_VALUE : 1)
			.filter(p -> p.toString().endsWith(".properties") && p.toFile().isFile())
			.map(Path::toFile)
			.iterator();
	}
}
