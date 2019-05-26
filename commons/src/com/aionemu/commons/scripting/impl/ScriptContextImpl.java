package com.aionemu.commons.scripting.impl;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.regex.PatternSyntaxException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.scripting.CompilationResult;
import com.aionemu.commons.scripting.ScriptCompiler;
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

	/**
	 * Script context that is parent for this script context
	 */
	private final ScriptContext parentScriptContext;

	/**
	 * Libraries (list of jar files) that have to be loaded class loader
	 */
	private Iterable<File> libraries;

	/**
	 * Root directories of this script context. It and it's subdirectories will be scanned for .java files.
	 */
	private final String dirPattern;

	/**
	 * Result of compilation of script context
	 */
	private CompilationResult compilationResult;

	/**
	 * List of child script contexts
	 */
	private Set<ScriptContext> childScriptContexts;

	/**
	 * Classlistener for this script context
	 */
	private ClassListener classListener;

	/**
	 * Creates new scriptcontext with given root file
	 * 
	 * @param dirPattern
	 *          directory pattern where java files will be loaded from
	 * @throws NullPointerException
	 *           if dirPattern is null
	 * @throws IllegalArgumentException
	 *           if no directory exists for dirPattern
	 */
	public ScriptContextImpl(String dirPattern) {
		this(dirPattern, null);
	}

	/**
	 * Creates new ScriptContext with given file as dirPattern and another ScriptContext as parent
	 * 
	 * @param dirPattern
	 *          directory pattern where java files will be loaded from
	 * @param parent
	 *          parent ScriptContex. It's classes and libraries will be accessible for this script context
	 * @throws NullPointerException
	 *           if dirPattern is null
	 * @throws IllegalArgumentException
	 *           if no directory exists for dirPattern
	 */
	public ScriptContextImpl(String dirPattern, ScriptContext parent) {
		this.dirPattern = dirPattern;
		this.parentScriptContext = parent;
		if (getDirectories().isEmpty())
			throw new IllegalArgumentException("No valid directories found for pattern: " + dirPattern);
	}

	@Override
	public synchronized void init() {

		if (compilationResult != null) {
			log.error("Init request on initialized ScriptContext");
			return;
		}

		ScriptCompiler scriptCompiler = new ScriptCompilerImpl();

		if (parentScriptContext != null) {
			scriptCompiler.setParentClassLoader(parentScriptContext.getCompilationResult().getClassLoader());
		}

		scriptCompiler.setLibraries(libraries);
		compilationResult = scriptCompiler.compile(findFiles());

		getClassListener().postLoad(compilationResult.getCompiledClasses());

		if (childScriptContexts != null) {
			for (ScriptContext context : childScriptContexts) {
				context.init();
			}
		}
	}

	private List<File> getDirectories() {
		List<File> files = new ArrayList<>();
		int globIndex = dirPattern.indexOf('*');
		if (globIndex == -1) {
			files.add(new File(dirPattern));
		} else {
			String rootDir = dirPattern.substring(0, globIndex);
			int lastSlashBeforeGlob = rootDir.lastIndexOf('/');
			if (lastSlashBeforeGlob > -1)
				rootDir = rootDir.substring(0, lastSlashBeforeGlob);
			String globPattern = dirPattern.substring(rootDir.length() + 1);
			try {
				Files.newDirectoryStream(Paths.get(rootDir), globPattern).forEach(path -> files.add(path.toFile()));
			} catch (PatternSyntaxException e) {
				throw new IllegalArgumentException("Root directory is not a valid directory pattern: " + dirPattern, e);
			} catch (IOException e) {
				throw new IllegalArgumentException("Couldn't match directory " + rootDir + " with pattern: " + dirPattern, e);
			}
		}
		files.removeIf(file -> !file.exists() || !file.isDirectory());
		return files;
	}

	private List<File> findFiles() {
		List<File> files = new ArrayList<>();
		for (File dir : getDirectories()) {
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

		if (compilationResult == null) {
			log.error("Shutdown of not initialized script context", new Exception());
			return;
		}

		if (childScriptContexts != null) {
			for (ScriptContext child : childScriptContexts) {
				child.shutdown();
			}
		}

		getClassListener().preUnload(compilationResult.getCompiledClasses());
		compilationResult = null;
	}

	@Override
	public void reload() {
		shutdown();
		init();
	}

	@Override
	public String getDirPattern() {
		return dirPattern;
	}

	@Override
	public CompilationResult getCompilationResult() {
		return compilationResult;
	}

	@Override
	public synchronized boolean isInitialized() {
		return compilationResult != null;
	}

	@Override
	public void setLibraries(Iterable<File> files) {
		this.libraries = files;
	}

	@Override
	public Iterable<File> getLibraries() {
		return libraries;
	}

	@Override
	public ScriptContext getParentScriptContext() {
		return parentScriptContext;
	}

	@Override
	public Collection<ScriptContext> getChildScriptContexts() {
		return childScriptContexts;
	}

	@Override
	public void addChildScriptContext(ScriptContext context) {

		synchronized (this) {
			if (childScriptContexts == null) {
				childScriptContexts = new HashSet<>();
			}

			if (childScriptContexts.contains(context)) {
				log.error("Double child definition, dirPattern: " + dirPattern + ", child: " + context.getDirPattern());
				return;
			}

			if (isInitialized()) {
				context.init();
			}
		}

		childScriptContexts.add(context);
	}

	@Override
	public void setClassListener(ClassListener cl) {
		classListener = cl;
	}

	@Override
	public ClassListener getClassListener() {
		if (classListener == null && getParentScriptContext() != null) {
			return getParentScriptContext().getClassListener();
		}
		return classListener;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof ScriptContextImpl)) {
			return false;
		}

		ScriptContextImpl another = (ScriptContextImpl) obj;

		if (parentScriptContext == null) {
			return another.getDirPattern().equals(dirPattern);
		}
		return another.getDirPattern().equals(dirPattern) && parentScriptContext.equals(another.parentScriptContext);
	}

	@Override
	public int hashCode() {
		return Objects.hash(parentScriptContext, dirPattern);
	}

	@Override
	public void finalize() throws Throwable {
		if (compilationResult != null) {
			log.error("Finalization of initialized ScriptContext. Forcing context shutdown.");
			shutdown();
		}
		super.finalize();
	}
}
