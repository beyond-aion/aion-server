package com.aionemu.commons.utils;

import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.ClassFileFormatVersion;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class contains utilities that are used when we are working with classes
 *
 * @author SoulKeeper
 */
public class ClassUtils {

	private static final Logger log = LoggerFactory.getLogger(ClassUtils.class);

	/**
	 * Checks if class in member of the package
	 *
	 * @param clazz
	 *          class to check
	 * @param packageName
	 *          package
	 * @return true if is member
	 */
	public static boolean isPackageMember(Class<?> clazz, String packageName) {
		return isPackageMember(clazz.getName(), packageName);
	}

	/**
	 * Checks if classNames belongs to package
	 *
	 * @param className
	 *          class name
	 * @param packageName
	 *          package
	 * @return true if belongs
	 */
	public static boolean isPackageMember(String className, String packageName) {
		if (!className.contains(".")) {
			return packageName == null || packageName.isEmpty();
		}
		String classPackage = className.substring(0, className.lastIndexOf('.'));
		return packageName.equals(classPackage);
	}

	/**
	 * Returns class names from directory.
	 *
	 * @param directory
	 *          folder with class files
	 * @return Set of fully qualified class names
	 * @throws IllegalArgumentException
	 *           if specified file is not directory or does not exists
	 * @throws NullPointerException
	 *           if directory is null
	 */
	public static Set<String> getClassNamesFromDirectory(File directory) throws IllegalArgumentException {

		if (!directory.isDirectory() || !directory.exists()) {
			throw new IllegalArgumentException("Directory " + directory + " doesn't exists or is not directory");
		}

		return getClassNamesFromPackage(directory, null, true);
	}

	/**
	 * Recursive method used to find all classes in a given directory and subdirs.
	 *
	 * @param directory
	 *          The base directory
	 * @param packageName
	 *          The package name for classes found inside the base directory
	 * @param recursive
	 *          include subpackages or not
	 * @return The classes
	 */
	public static Set<String> getClassNamesFromPackage(File directory, String packageName, boolean recursive) {
		if (!directory.exists())
			return Collections.emptySet();

		File[] files = directory.listFiles();
		if (files == null || files.length == 0)
			return Collections.emptySet();

		Set<String> classes = new HashSet<>();
		for (File file : files) {
			if (file.isDirectory()) {

				if (!recursive) {
					continue;
				}

				String newPackage = file.getName();
				if (!GenericValidator.isBlankOrNull(packageName)) {
					newPackage = packageName + "." + newPackage;
				}
				classes.addAll(getClassNamesFromPackage(file, newPackage, recursive));
			} else if (file.getName().endsWith(".class")) {
				String className = file.getName().substring(0, file.getName().length() - 6);
				if (!GenericValidator.isBlankOrNull(packageName)) {
					className = packageName + "." + className;
				}
				classes.add(className);
			}
		}
		return classes;
	}

	/**
	 * Method that returns all class file names from given jar file
	 *
	 * @param file
	 *          jar file
	 * @return class names from jar file
	 * @throws IOException
	 *           if something went wrong
	 * @throws IllegalArgumentException
	 *           if file doesn't exists or is not jar file
	 * @throws NullPointerException
	 *           if file is null
	 */
	public static Set<String> getClassNamesFromJarFile(File file) throws IOException {

		if (!file.exists() || file.isDirectory()) {
			throw new IllegalArgumentException("File " + file + " is not valid jar file");
		}

		Set<String> result = new HashSet<>();

		JarFile jarFile = null;
		try {
			jarFile = new JarFile(file);

			Enumeration<JarEntry> entries = jarFile.entries();
			while (entries.hasMoreElements()) {
				JarEntry entry = entries.nextElement();

				String name = entry.getName();
				if (name.endsWith(".class")) {
					name = name.substring(0, name.length() - 6);
					name = name.replace('/', '.');
					result.add(name);
				}
			}
		} finally {
			if (jarFile != null) {
				try {
					jarFile.close();
				} catch (IOException e) {
					log.error("Failed to close jar file " + jarFile.getName(), e);
				}
			}
		}

		return result;
	}

	public static ClassFileFormatVersion readClassFileVersion(InputStream classFileInputStream, String fileName) throws IOException {
		try (DataInputStream input = new DataInputStream(classFileInputStream)) {
			// The first 4 bytes of a .class file are 0xCAFEBABE and are "used to identify file as conforming to the class file format"
			String firstFourBytes = Integer.toHexString(input.readUnsignedShort()) + Integer.toHexString(input.readUnsignedShort());
			if (!firstFourBytes.equalsIgnoreCase("cafebabe")) {
				throw new IllegalArgumentException(fileName + " is not a Java .class file.");
			}
			int minorVersion = input.readUnsignedShort();
			int majorVersion = input.readUnsignedShort();
			return ClassFileFormatVersion.fromMajor(majorVersion);
		}
	}
}
