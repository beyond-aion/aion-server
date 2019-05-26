package com.aionemu.commons.scripting;

import java.io.File;

/**
 * This interface represents common functionality list that should be available for any compiler that is going to be used with scripting engine. For
 * instance, groovy can be used, however it produces by far not the best bytecode so by default javac from sun is used.
 * 
 * @author SoulKeeper
 */
public interface ScriptCompiler {

	/**
	 * Sets parent class loader for this compiler.<br>
	 * <br>
	 * <font color="red">Warning, for now only</font>
	 * 
	 * @param classLoader
	 *          ScriptClassLoader that will be used as parent
	 */
	void setParentClassLoader(ScriptClassLoader classLoader);

	/**
	 * List of jar files that are required for compilation
	 * 
	 * @param files
	 *          list of jar files
	 */
	void setLibraries(Iterable<File> files);

	/**
	 * Compiles single class that is represented as string
	 * 
	 * @param className
	 *          class name
	 * @param sourceCode
	 *          class sourse code
	 * @return {@link com.aionemu.commons.scripting.CompilationResult}
	 */
	CompilationResult compile(String className, String sourceCode);

	/**
	 * Compiles classes that are represented as strings
	 * 
	 * @param className
	 *          class names
	 * @param sourceCode
	 *          class sources
	 * @return {@link com.aionemu.commons.scripting.CompilationResult}
	 * @throws IllegalArgumentException
	 *           if number of class names != number of sources
	 */
	CompilationResult compile(String[] className, String[] sourceCode) throws IllegalArgumentException;

	/**
	 * Compiles list of files
	 * 
	 * @param compilationUnits
	 *          list of files
	 * @return {@link com.aionemu.commons.scripting.CompilationResult}
	 */
	CompilationResult compile(Iterable<File> compilationUnits);
}
