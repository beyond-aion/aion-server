package com.aionemu.commons.scripting.impl.javacompiler;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;

import javax.tools.*;
import javax.tools.JavaFileObject.Kind;

import com.aionemu.commons.scripting.ScriptClassLoader;

/**
 * This class extends manages loaded classes. It is also responsible for tricking compiler. Unfortunally compiler doen't work with classloaders, so we
 * have to pass class data manually for each compilation.
 *
 * @author SoulKeeper
 */
public class ClassFileManager extends ForwardingJavaFileManager<JavaFileManager> {

	/**
	 * This map contains classes compiled for this classloader
	 */
	private final Map<String, BinaryClass> compiledClasses = new HashMap<>();

	/**
	 * Classloader that will be used to load compiled classes
	 */
	protected ScriptClassLoaderImpl loader;

	/**
	 * Parent classloader for loader
	 */
	protected ScriptClassLoader parentClassLoader;

	/**
	 * Creates new ClassFileManager.
	 *
	 * @param compiler
	 *          that will be used
	 * @param listener
	 *          class that will report compilation errors
	 */
	public ClassFileManager(JavaCompiler compiler, DiagnosticListener<? super JavaFileObject> listener) {
		super(compiler.getStandardFileManager(listener, null, null));
	}

	/**
	 * Returns JavaFileObject that will be used to write class data into it by compiler
	 *
	 * @param location
	 *          not used
	 * @param className
	 *          JavaFileObject will have this className
	 * @param kind
	 *          not used
	 * @param sibling
	 *          source file for the class
	 * @return JavaFileObject that will be used to store compiled class data
	 */
	@Override
	public JavaFileObject getJavaFileForOutput(Location location, String className, Kind kind, FileObject sibling) {
		BinaryClass co = new BinaryClass(className, sibling);
		compiledClasses.put(className, co);
		return co;
	}

	/**
	 * Returns or creates a classloader for this ClassFileManager.
	 *
	 * @param location
	 *          not used
	 * @return classLoader of this ClassFileManager
	 */
	@Override
	public synchronized ScriptClassLoaderImpl getClassLoader(Location location) {
		if (loader == null) {
			if (parentClassLoader != null) {
				loader = new ScriptClassLoaderImpl(this, parentClassLoader);
			} else {
				loader = new ScriptClassLoaderImpl(this);
			}
		}
		return loader;
	}

	/**
	 * Sets parentClassLoader for this classLoader
	 *
	 * @param classLoader
	 *          parent class loader
	 */
	public void setParentClassLoader(ScriptClassLoader classLoader) {
		this.parentClassLoader = classLoader;
	}

	public void addClass(String className, File classFile) {
		try {
			byte[] bytes = Files.readAllBytes(classFile.toPath());
			getJavaFileForOutput(null, className, null, null).openOutputStream().write(bytes);
		} catch (IOException e) {
			throw new RuntimeException("Couldn't load cached class " + classFile, e);
		}
	}

	/**
	 * Returns list of classes that were compiled by compiler related to this ClassFileManager
	 *
	 * @return list of classes
	 */
	public Map<String, BinaryClass> getCompiledClasses() {
		return compiledClasses;
	}

	/**
	 * This method overrides class resolving procedure for compiler. It uses classloaders to resolve classes that compiler may need during compilation.
	 * Compiler by itself can't detect them. So we have to use this hack here. Hack is used only if compiler requests for classes in classpath.
	 *
	 * @param location
	 *          Location to search classes
	 * @param packageName
	 *          package to scan for classes
	 * @param kinds
	 *          FileTypes to search
	 * @param recurse
	 *          not used
	 * @return list of required files
	 * @throws IOException
	 *           if something foes wrong
	 */
	@Override
	public Iterable<JavaFileObject> list(Location location, String packageName, Set<Kind> kinds, boolean recurse) throws IOException {
		Iterable<JavaFileObject> objects = super.list(location, packageName, kinds, recurse);

		if (StandardLocation.CLASS_PATH.equals(location) && kinds.contains(Kind.CLASS)) {
			List<JavaFileObject> temp = new ArrayList<>();
			for (JavaFileObject object : objects) {
				temp.add(object);
			}

			temp.addAll(loader.getClassesForPackage(packageName));
			objects = temp;
		}

		return objects;
	}

	@Override
	public String inferBinaryName(Location location, JavaFileObject file) {
		if (file instanceof BinaryClass) {
			return file.getName();
		}

		return super.inferBinaryName(location, file);
	}

}
