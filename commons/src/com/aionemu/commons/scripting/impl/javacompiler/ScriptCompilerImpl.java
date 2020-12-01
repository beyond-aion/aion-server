package com.aionemu.commons.scripting.impl.javacompiler;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.tools.DiagnosticListener;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.ToolProvider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.scripting.CompilationResult;
import com.aionemu.commons.scripting.ScriptClassLoader;
import com.aionemu.commons.scripting.ScriptCompiler;

/**
 * Wrapper for JavaCompiler api
 * 
 * @author SoulKeeper
 */
public class ScriptCompilerImpl implements ScriptCompiler {

	private static final Logger log = LoggerFactory.getLogger(ScriptCompilerImpl.class);
	private static final List<String> COMPILER_OPTIONS = new ArrayList<>();

	static {
		RuntimeMXBean runtimeMxBean = ManagementFactory.getRuntimeMXBean();
		COMPILER_OPTIONS.addAll(Arrays.asList("-encoding", "UTF-8", "-g"));
		if (runtimeMxBean.getInputArguments().contains("--enable-preview")) {
			// activate preview features in compiler too, if the project was started with --enable-preview
			COMPILER_OPTIONS.add("--enable-preview");
			COMPILER_OPTIONS.add("--release");
			COMPILER_OPTIONS.add(runtimeMxBean.getSpecVersion());
		}
	}

	/**
	 * Instance of JavaCompiler that will be used to compile classes
	 */
	protected final JavaCompiler javaCompiler;

	/**
	 * List of class files
	 */
	protected Map<String, File> classFiles;

	/**
	 * Parent classloader that has to be used for this compiler
	 */
	protected ScriptClassLoader parentClassLoader;

	/**
	 * @throws RuntimeException
	 *           if compiler is not available
	 */
	public ScriptCompilerImpl() {
		javaCompiler = ToolProvider.getSystemJavaCompiler();
		if (javaCompiler == null) {
			log.info("JAVA_HOME={}", System.getenv("JAVA_HOME"));
			throw new IllegalStateException("Could not find compiler! Make sure javac is in PATH.");
		}
	}

	/**
	 * Sets parent classLoader for this JavaCompilerImpl
	 * 
	 * @param classLoader
	 *          parent classloader
	 */
	@Override
	public void setParentClassLoader(ScriptClassLoader classLoader) {
		this.parentClassLoader = classLoader;
	}

	@Override
	public void setClasses(Map<String, File> classFiles) {
		this.classFiles = classFiles;
	}

	/**
	 * Compiles given class.
	 * 
	 * @param className
	 *          Name of the class
	 * @param sourceCode
	 *          source code
	 * @return CompilationResult with the class
	 * @throws RuntimeException
	 *           if compilation failed with errors
	 */
	@Override
	public CompilationResult compile(String className, String sourceCode) {
		return compile(new String[] { className }, new String[] { sourceCode });
	}

	/**
	 * Compiles list of classes. Amount of classNames must be equal to amount of sourceCodes
	 * 
	 * @param classNames
	 *          classNames
	 * @param sourceCode
	 *          list of source codes
	 * @return CompilationResult with needed files
	 * @throws IllegalArgumentException
	 *           if size of classNames not equals to size of sourceCodes
	 * @throws RuntimeException
	 *           if compilation failed with errors
	 */
	@Override
	public CompilationResult compile(String[] classNames, String[] sourceCode) throws IllegalArgumentException {

		if (classNames.length != sourceCode.length) {
			throw new IllegalArgumentException("Amount of classes is not equal to amount of sources");
		}

		List<JavaFileObject> compilationUnits = new ArrayList<>();

		for (int i = 0; i < classNames.length; i++) {
			JavaFileObject compilationUnit = new JavaSourceFromString(classNames[i], sourceCode[i]);
			compilationUnits.add(compilationUnit);
		}

		return doCompilation(compilationUnits);
	}

	/**
	 * Compiles given files. Files must be java sources.
	 * 
	 * @param compilationUnits
	 *          files to compile
	 * @return CompilationResult with classes
	 * @throws RuntimeException
	 *           if compilation failed with errors
	 */
	@Override
	public CompilationResult compile(Iterable<File> compilationUnits) {
		List<JavaFileObject> list = new ArrayList<>();

		for (File f : compilationUnits) {
			list.add(new JavaSourceFromFile(f, JavaFileObject.Kind.SOURCE));
		}

		return doCompilation(list);
	}

	/**
	 * Actually performs compilation. Compiler expects sources in UTF-8 encoding. Also compiler generates full debugging info for classes.
	 * 
	 * @param compilationUnits
	 *          Units that will be compiled
	 * @return CompilationResult with compiledClasses
	 * @throws RuntimeException
	 *           if compilation failed with errors
	 */
	protected CompilationResult doCompilation(Iterable<JavaFileObject> compilationUnits) {
		DiagnosticListener<JavaFileObject> listener = new ErrorListener();
		ClassFileManager manager = new ClassFileManager(javaCompiler, listener);
		manager.setParentClassLoader(parentClassLoader);

		if (classFiles != null)
			classFiles.forEach(manager::addClass);

		if (compilationUnits.iterator().hasNext()) {
			JavaCompiler.CompilationTask task = javaCompiler.getTask(null, manager, listener, COMPILER_OPTIONS, null, compilationUnits);

			if (!task.call())
				throw new RuntimeException("Error while compiling classes");
		}

		ScriptClassLoader cl = manager.getClassLoader(null);
		return new CompilationResult(manager.getCompiledClasses().values(), cl);
	}
}
