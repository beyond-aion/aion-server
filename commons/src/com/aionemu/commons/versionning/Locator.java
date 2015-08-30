package com.aionemu.commons.versionning;

import java.io.File;
import java.io.FilenameFilter;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;

/**
 * The Locator is a utility class which is used to find certain items in the environment.
 * 
 * @since Ant 1.6
 */
public final class Locator {

	/**
	 * Not instantiable
	 */
	private Locator() {
	}

	/**
	 * Find the directory or jar file the class has been loaded from.
	 * 
	 * @param c
	 *          the class whose location is required.
	 * @return the file or jar with the class or null if we cannot determine the location.
	 * @since Ant 1.6
	 */
	public static File getClassSource(Class<?> c) {
		String classResource = c.getName().replace('.', '/') + ".class";
		return getResourceSource(c.getClassLoader(), classResource);
	}

	/**
	 * Find the directory or jar a given resource has been loaded from.
	 * 
	 * @param c
	 *          the classloader to be consulted for the source.
	 * @param resource
	 *          the resource whose location is required.
	 * @return the file with the resource source or null if we cannot determine the location.
	 * @since Ant 1.6
	 */
	public static File getResourceSource(ClassLoader c, String resource) {
		if (c == null) {
			c = Locator.class.getClassLoader();
		}
		URL url = null;
		if (c == null) {
			url = ClassLoader.getSystemResource(resource);
		}
		else {
			url = c.getResource(resource);
		}
		if (url != null) {
			String u = url.toString();
			if (u.startsWith("jar:file:")) {
				int pling = u.indexOf("!");
				String jarName = u.substring(4, pling);
				return new File(fromURI(jarName));
			}
			else if (u.startsWith("file:")) {
				int tail = u.indexOf(resource);
				String dirName = u.substring(0, tail);
				return new File(fromURI(dirName));
			}
		}
		return null;
	}

	/**
	 * Constructs a file path from a <code>file:</code> URI.
	 * <p>
	 * Will be an absolute path if the given URI is absolute.
	 * </p>
	 * <p>
	 * Swallows '%' that are not followed by two characters, doesn't deal with non-ASCII characters.
	 * </p>
	 * 
	 * @param uri
	 *          the URI designating a file in the local filesystem.
	 * @return the local file system path for the file.
	 * @since Ant 1.6
	 */
	public static String fromURI(String uri) {
		URL url = null;
		try {
			url = new URL(uri);
		}
		catch (MalformedURLException emYouEarlEx) {
			// Ignore malformed exception
		}
		if (url == null || !("file".equals(url.getProtocol()))) {
			throw new IllegalArgumentException("Can only handle valid file: URIs");
		}
		StringBuffer buf = new StringBuffer(url.getHost());
		if (buf.length() > 0) {
			buf.insert(0, File.separatorChar).insert(0, File.separatorChar);
		}
		String file = url.getFile();
		int queryPos = file.indexOf('?');
		buf.append((queryPos < 0) ? file : file.substring(0, queryPos));

		uri = buf.toString().replace('/', File.separatorChar);

		if (File.pathSeparatorChar == ';' && uri.startsWith("\\") && uri.length() > 2 && Character.isLetter(uri.charAt(1))
			&& uri.lastIndexOf(':') > -1) {
			uri = uri.substring(1);
		}
		String path = decodeUri(uri);
		return path;
	}

	/**
	 * Decodes an Uri with % characters.
	 * 
	 * @param uri
	 *          String with the uri possibly containing % characters.
	 * @return The decoded Uri
	 */
	private static String decodeUri(String uri) {
		if (uri.indexOf('%') == -1) {
			return uri;
		}
		StringBuffer sb = new StringBuffer();
		CharacterIterator iter = new StringCharacterIterator(uri);
		for (char c = iter.first(); c != CharacterIterator.DONE; c = iter.next()) {
			if (c == '%') {
				char c1 = iter.next();
				if (c1 != CharacterIterator.DONE) {
					int i1 = Character.digit(c1, 16);
					char c2 = iter.next();
					if (c2 != CharacterIterator.DONE) {
						int i2 = Character.digit(c2, 16);
						sb.append((char) ((i1 << 4) + i2));
					}
				}
			}
			else {
				sb.append(c);
			}
		}
		String path = sb.toString();
		return path;
	}

	/**
	 * Get an array of URLs representing all of the jar files in the given location. If the location is a file, it is
	 * returned as the only element of the array. If the location is a directory, it is scanned for jar files.
	 * 
	 * @param location
	 *          the location to scan for Jars.
	 * @return an array of URLs for all jars in the given location.
	 * @exception MalformedURLException
	 *              if the URLs for the jars cannot be formed.
	 */
	public static URL[] getLocationURLs(File location) throws MalformedURLException {
		return getLocationURLs(location, new String[] { ".jar" });
	}

	/**
	 * Get an array of URLs representing all of the files of a given set of extensions in the given location. If the
	 * location is a file, it is returned as the only element of the array. If the location is a directory, it is scanned
	 * for matching files.
	 * 
	 * @param location
	 *          the location to scan for files.
	 * @param extensions
	 *          an array of extension that are to match in the directory search.
	 * @return an array of URLs of matching files.
	 * @exception MalformedURLException
	 *              if the URLs for the files cannot be formed.
	 */
	public static URL[] getLocationURLs(File location, final String[] extensions) throws MalformedURLException {

		URL[] urls = new URL[0];

		if (!location.exists()) {
			return urls;
		}
		if (!location.isDirectory()) {
			urls = new URL[1];
			String path = location.getPath();
			for (int i = 0; i < extensions.length; ++i) {
				if (path.toLowerCase().endsWith(extensions[i])) {
					urls[0] = location.toURI().toURL();
					break;
				}
			}
			return urls;
		}
		File[] matches = location.listFiles(new FilenameFilter() {

			@Override
			public boolean accept(File dir, String name) {
				for (int i = 0; i < extensions.length; ++i) {
					if (name.toLowerCase().endsWith(extensions[i])) {
						return true;
					}
				}
				return false;
			}
		});
		urls = new URL[matches.length];
		for (int i = 0; i < matches.length; ++i) {
			urls[i] = matches[i].toURI().toURL();
		}
		return urls;
	}
}
