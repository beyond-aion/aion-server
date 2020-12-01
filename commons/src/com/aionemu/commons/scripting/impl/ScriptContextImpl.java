package com.aionemu.commons.scripting.impl;

import java.io.File;
import java.io.IOException;
import java.lang.ref.Cleaner;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.configs.CommonsConfig;
import com.aionemu.commons.scripting.CompilationResult;
import com.aionemu.commons.scripting.ScriptCompiler;
import com.aionemu.commons.scripting.ScriptCompilerCache;
import com.aionemu.commons.scripting.ScriptContext;
import com.aionemu.commons.scripting.classlistener.ClassListener;
import com.aionemu.commons.scripting.impl.javacompiler.ScriptCompilerImpl;

/**
 * This class is actual implementation of {@link com.aionemu.commons.scripting.ScriptContext}
 * 
 * @author SoulKeeper
 */
public class ScriptContextImpl implements ScriptContext {

	/**
	 * logger for this class
	 */
	private static final Logger log = LoggerFactory.getLogger(ScriptContextImpl.class);
	private final static Cleaner CLEANER = Cleaner.create();

	/**
	 * Root directories of this script context. It and it's subdirectories will be scanned for .java files.
	 */
	private final File[] directories;

	private final CleanableState state;

	private static class CleanableState implements Runnable {

		/**
		 * Result of compilation of script context
		 */
		private CompilationResult compilationResult;

		/**
		 * Classlistener for this script context
		 */
		private ClassListener classListener;

		@Override
		public void run() {
			if (compilationResult != null) {
				log.error("Finalization of initialized ScriptContext. Forcing context shutdown.");
				shutdown();
			}
		}

		private synchronized void shutdown() {
			if (compilationResult == null) {
				log.error("Shutdown of not initialized script context", new Exception());
				return;
			}
			getClassListener().preUnload(compilationResult.getCompiledClasses());
			compilationResult = null;
		}

		private ClassListener getClassListener() {
			return classListener;
		}
	}

	/**
	 * Creates new scriptcontext with given root file
	 * 
	 * @param directories
	 *          directories where java files will be loaded from (recursively)
	 * @throws NullPointerException
	 *           if dirPattern is null
	 * @throws IllegalArgumentException
	 *           if no directory exists for dirPattern
	 */
	public ScriptContextImpl(File... directories) {
		if (directories.length == 0 || !Stream.of(directories).allMatch(File::isDirectory))
			throw new IllegalArgumentException("Invalid directories given: " + Arrays.toString(directories));
		this.directories = directories;
		this.state = new CleanableState();
		CLEANER.register(this, this.state);
	}

	@Override
	public synchronized void init() {
		if (state.compilationResult != null) {
			log.error("Init request on initialized ScriptContext");
			return;
		}

		ScriptCompiler scriptCompiler = new ScriptCompilerImpl();
		List<File> sourceFiles = findFiles();
		if (CommonsConfig.SCRIPT_COMPILER_CACHING)
			scriptCompiler.setClasses(ScriptCompilerCache.findValidCachedClassFiles(sourceFiles));
		state.compilationResult = scriptCompiler.compile(sourceFiles);
		if (CommonsConfig.SCRIPT_COMPILER_CACHING)
			ScriptCompilerCache.cacheClasses(state.compilationResult.getBinaryClasses());

		getClassListener().postLoad(state.compilationResult.getCompiledClasses());
	}

	private List<File> findFiles() {
		List<File> files = new ArrayList<>();
		for (File dir : directories) {
			try {
				Files.find(dir.toPath(), Integer.MAX_VALUE, (path, attrs) -> attrs.isRegularFile() && path.toString().endsWith(".java"))
					.forEach(path -> files.add(path.toFile()));
			} catch (IOException e) {
				throw new RuntimeException("Error scanning " + dir, e);
			}
		}
		return files;
	}

	@Override
	public synchronized void shutdown() {
		state.shutdown();
	}

	@Override
	public void reload() {
		shutdown();
		init();
	}

	@Override
	public CompilationResult getCompilationResult() {
		return state.compilationResult;
	}

	@Override
	public synchronized boolean isInitialized() {
		return state.compilationResult != null;
	}

	@Override
	public void setClassListener(ClassListener cl) {
		state.classListener = cl;
	}

	@Override
	public ClassListener getClassListener() {
		return state.getClassListener();
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof ScriptContextImpl another && Arrays.equals(another.directories, directories);
	}

	@Override
	public int hashCode() {
		return Arrays.hashCode(directories);
	}
}
