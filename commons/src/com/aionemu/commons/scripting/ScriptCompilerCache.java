package com.aionemu.commons.scripting;

import java.io.*;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.configs.CommonsConfig;
import com.aionemu.commons.configuration.ConfigurableProcessor;
import com.aionemu.commons.configuration.Properties;
import com.aionemu.commons.scripting.impl.javacompiler.BinaryClass;
import com.aionemu.commons.utils.PropertiesUtils;

/**
 * This class holds file references to compiled classes. Script compiler may use it to load/store compiled classes from/to disk to accelerate class
 * loading.
 * 
 * @author Neon
 */
public class ScriptCompilerCache {

	private static final Logger log = LoggerFactory.getLogger(ScriptCompilerCache.class);
	private static final char FILE_SEPARATOR = System.getProperty("file.separator").charAt(0);
	private static final Path WORKING_DIR = Paths.get("").toAbsolutePath();
	public static final Path CACHE_DIR = Paths.get("./cache/classes");
	private static final File CACHE_CLASS_MAP = new File(CACHE_DIR + "/classes.properties");
	private static final AtomicInteger ACCESSORS = new AtomicInteger();
	private static final AtomicBoolean SHOULD_PERSIST = new AtomicBoolean();

	@Properties
	private static ConcurrentHashMap<File, Set<File>> CLASS_FILES_BY_SOURCE_FILE;

	static {
		try {
			java.util.Properties properties = PropertiesUtils.load(CACHE_CLASS_MAP, null);
			ConfigurableProcessor.process(properties, ScriptCompilerCache.class);
			if (CommonsConfig.SCRIPT_COMPILER_CACHING) {
				if (!CACHE_CLASS_MAP.exists() || !CLASS_FILES_BY_SOURCE_FILE.keySet().stream().allMatch(File::isFile)) {
					if (CACHE_DIR.toFile().exists())
						Files.walk(CACHE_DIR).sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
					CLASS_FILES_BY_SOURCE_FILE.clear();
					log.info("Rebuilding compiled class cache because some files got deleted or renamed");
				} else {
					log.info("Initialized compiled class cache with " + CLASS_FILES_BY_SOURCE_FILE.size() + " classes");
				}
			}
		} catch (IOException e) {
			throw new ExceptionInInitializerError(e);
		}
	}

	public static synchronized void invalidate(List<File> sourceFiles) {
		ACCESSORS.incrementAndGet();
		sourceFiles.forEach(CLASS_FILES_BY_SOURCE_FILE::remove);
		if (ACCESSORS.decrementAndGet() == 0 && SHOULD_PERSIST.get())
			saveClassFileMap();
	}

	public static Map<String, File> findValidCachedClassFiles(List<File> sourceFiles) {
		ACCESSORS.incrementAndGet();
		if (!CACHE_DIR.toFile().exists())
			return Collections.emptyMap();
		Map<String, File> cachedClasses = new HashMap<>();
		for (Iterator<File> iter = sourceFiles.iterator(); iter.hasNext();) {
			List<File> classFiles = findValidCachedClassFiles(iter.next()); // one source file can yield multiple class files (inner classes etc.)
			if (classFiles.isEmpty())
				continue;
			for (File classFile : classFiles)
				cachedClasses.put(createClassName(classFile), classFile);
			iter.remove();
		}
		return cachedClasses;
	}

	public static void cacheClasses(Collection<BinaryClass> binaryClasses) {
		binaryClasses.parallelStream().forEach(binaryClass -> {
			if (binaryClass.getSourceFile() == null) // class was loaded from cache, source file is already in cache map
				return;
			try {
				Path path = Paths.get(CACHE_DIR.toString(), binaryClass.toUri().toString().replace('.', '/') + ".class");
				Files.createDirectories(path.getParent());
				Path classFile = Files.write(path, binaryClass.getBytes());
				addClassFile(binaryClass.getSourceFile(), classFile);
				SHOULD_PERSIST.set(true);
			} catch (IOException e) {
				log.error("Couldn't cache " + binaryClass.getName(), e);
			}
		});
		if (ACCESSORS.decrementAndGet() == 0 && SHOULD_PERSIST.get()) // only save once after concurrent access finished and only if cache changed
			saveClassFileMap();
	}

	public static boolean contains(String className) {
		String fileName = className.replace('.', FILE_SEPARATOR) + ".class";
		return CLASS_FILES_BY_SOURCE_FILE.values().stream().anyMatch(files -> files.stream().anyMatch(file -> file.getPath().endsWith(fileName)));
	}

	private static List<File> findValidCachedClassFiles(File sourceFile) {
		List<File> classFiles = getClassFiles(sourceFile);
		for (File classFile : classFiles) {
			if (!classFile.isFile() || classFile.lastModified() < sourceFile.lastModified()) {
				CLASS_FILES_BY_SOURCE_FILE.remove(sourceFile);
				return Collections.emptyList();
			}
		}
		return classFiles;
	}

	private static String createClassName(File classFile) {
		String className = CACHE_DIR.relativize(classFile.toPath()).toString();
		className = className.substring(0, className.length() - 6);
		return className.replace(FILE_SEPARATOR, '.');
	}

	private static List<File> getClassFiles(File sourceFile) {
		sourceFile = WORKING_DIR.relativize(Paths.get(sourceFile.toURI())).toFile();
		Set<File> classFiles = CLASS_FILES_BY_SOURCE_FILE.get(sourceFile);
		if (classFiles == null)
			return Collections.emptyList();
		return classFiles.stream().map(file -> Paths.get(CACHE_DIR.toString(), file.toString()).toFile()).collect(Collectors.toList());
	}

	private static void addClassFile(URI sourceFileUri, Path classFile) {
		File sourceFile = WORKING_DIR.relativize(Paths.get(sourceFileUri)).toFile();
		CLASS_FILES_BY_SOURCE_FILE.compute(sourceFile, (key, files) -> {
			if (files == null)
				files = ConcurrentHashMap.newKeySet();
			files.add(CACHE_DIR.relativize(classFile).toFile());
			return files;
		});
	}

	private static synchronized boolean saveClassFileMap() {
		java.util.Properties properties = new java.util.Properties();
		CLASS_FILES_BY_SOURCE_FILE.forEach(
			(sourceFile, classFiles) -> properties.put(sourceFile.toString(), classFiles.stream().map(File::getPath).collect(Collectors.joining(","))));
		try (OutputStream os = new BufferedOutputStream(new FileOutputStream(CACHE_CLASS_MAP))) {
			properties.store(os, "THIS FILE IS AUTO GENERATED, DO NOT EDIT!");
		} catch (IOException e) {
			log.error("Couldn't save class file map", e);
			return false;
		}
		SHOULD_PERSIST.set(false);
		return true;
	}
}
