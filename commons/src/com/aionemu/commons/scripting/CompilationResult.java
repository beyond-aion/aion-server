package com.aionemu.commons.scripting;

import java.util.Collection;

import com.aionemu.commons.scripting.impl.javacompiler.BinaryClass;

/**
 * This class represents compilation result of script context
 * 
 * @author SoulKeeper
 */
public class CompilationResult {

	/**
	 * List of classes that were compiled by compiler
	 */
	private final Collection<BinaryClass> binaryClasses;

	/**
	 * Classloader that was used to load classes
	 */
	private final ScriptClassLoader classLoader;

	/**
	 * Creates new instance of CompilationResult with classes that has to be parsed and classloader that was used to load classes
	 * 
	 * @param binaryClasses
	 *          classes compiled by compiler
	 * @param classLoader
	 *          classloader that was used by compiler
	 */
	public CompilationResult(Collection<BinaryClass> binaryClasses, ScriptClassLoader classLoader) {
		this.binaryClasses = binaryClasses;
		this.classLoader = classLoader;
		loadClasses();
	}

	public Collection<BinaryClass> getBinaryClasses() {
		return binaryClasses;
	}

	/**
	 * @return classloader that was used by compiler
	 */
	public ScriptClassLoader getClassLoader() {
		return classLoader;
	}

	/**
	 * @return list of classes that were compiled
	 */
	public Class<?>[] getCompiledClasses() {
		return binaryClasses.stream().map(BinaryClass::getDefinedClass).toArray(Class[]::new);
	}

	private void loadClasses() {
		for (BinaryClass binaryClass : binaryClasses) {
			try {
				if (binaryClass.getDefinedClass() == null)
					classLoader.loadClass(binaryClass.getName());
				if (binaryClass.getDefinedClass() == null) // should not happen
					throw new RuntimeException("Class " + binaryClass.getName() + " was loaded but not set, check your ScriptClassLoader");
			} catch (ClassNotFoundException e) {
				throw new RuntimeException(e);
			}
		}
	}

	@Override
	public String toString() {
		return "CompilationResult{binaryClasses=" + binaryClasses + ", classLoader=" + classLoader + '}';
	}
}
