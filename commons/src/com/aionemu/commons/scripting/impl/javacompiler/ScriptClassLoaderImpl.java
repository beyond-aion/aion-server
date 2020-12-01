package com.aionemu.commons.scripting.impl.javacompiler;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

import javax.tools.JavaFileObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.scripting.ScriptClassLoader;
import com.aionemu.commons.utils.ClassUtils;

/**
 * This classloader is used to load script classes. <br>
 * <br>
 * Due to JavaCompiler limitations we have to keep list of available classes here.
 * 
 * @author SoulKeeper
 */
public class ScriptClassLoaderImpl extends ScriptClassLoader {

	private static final Logger log = LoggerFactory.getLogger(ScriptClassLoaderImpl.class);

	/**
	 * ClassFileManager that is related to this ClassLoader
	 */
	private final ClassFileManager classFileManager;

	/**
	 * Creates new ScriptClassLoader with given ClassFileManger. <br>
	 * Parent ClassLoader is ClassLoader of current class:
	 * 
	 * <pre>
	 * ScriptClassLoaderImpl.class.getClassLoader()
	 * </pre>
	 * 
	 * @param classFileManager
	 *          classFileManager of this classLoader
	 */
	ScriptClassLoaderImpl(ClassFileManager classFileManager) {
		super(new URL[] {}, ScriptClassLoaderImpl.class.getClassLoader());
		this.classFileManager = classFileManager;
	}

	/**
	 * Creates new ScriptClassLoader with given ClassFileManger and another classLoader as parent
	 * 
	 * @param classFileManager
	 *          classFileManager of this classLoader
	 * @param parent
	 *          parent classLoader
	 */
	ScriptClassLoaderImpl(ClassFileManager classFileManager, ClassLoader parent) {
		super(new URL[] {}, parent);
		this.classFileManager = classFileManager;
	}

	/**
	 * Returns ClassFileManager that is related to this ClassLoader
	 * 
	 * @return classFileManager of this classLoader
	 */
	public ClassFileManager getClassFileManager() {
		return classFileManager;
	}

	@Override
	public Set<String> getCompiledClasses() {
		Set<String> compiledClasses = classFileManager.getCompiledClasses().keySet();
		return Collections.unmodifiableSet(compiledClasses);
	}

	/**
	 * Returns list of classes that are members of a package
	 * 
	 * @param packageName
	 *          package to search for classes
	 * @return list of classes that are package members
	 * @throws IOException
	 *           if was unable to load class
	 */
	public Set<JavaFileObject> getClassesForPackage(String packageName) throws IOException {
		Set<JavaFileObject> result = new HashSet<>();

		// load parent
		ClassLoader parent = getParent();
		if (parent instanceof ScriptClassLoaderImpl) {
			try (ScriptClassLoaderImpl pscl = (ScriptClassLoaderImpl) parent) {
				result.addAll(pscl.getClassesForPackage(packageName));
			}
		}

		// load current classloader compiled classes
		for (String cn : classFileManager.getCompiledClasses().keySet()) {
			if (ClassUtils.isPackageMember(cn, packageName)) {
				BinaryClass bc = classFileManager.getCompiledClasses().get(cn);
				result.add(bc);
			}
		}

		// initialize set with class names, will be used to resolve classes
		Set<String> classNames = new HashSet<>();

		// load package members from this classloader
		Enumeration<URL> urls = getResources(packageName.replace('.', '/'));
		while (urls.hasMoreElements()) {
			URL url = urls.nextElement();
			File file = new File(url.getPath());
			if (file.isDirectory()) {
				Set<String> packageClasses = ClassUtils.getClassNamesFromPackage(file, packageName, false);
				classNames.addAll(packageClasses);
			} else if (file.getName().toLowerCase().contains(".jar!")) {
				while (file.getParentFile() != null && !file.getName().toLowerCase().endsWith(".jar!")) {
					file = file.getParentFile();
				}
				// add jar file as library. Actually it's doesn't matter if we have it as library or as file in class path
				classNames.addAll(ClassUtils.getClassNamesFromJarFile(file));
			}
		}

		// load classes for class names from this classloader
		for (String cn : classNames) {
			if (ClassUtils.isPackageMember(cn, packageName)) {
				BinaryClass bc = new BinaryClass(cn, null);
				try {
					loadRawClassByName(cn, bc);
				} catch (IOException e) {
					log.error("Error while loading class from package " + packageName, e);
					throw e;
				}
				result.add(bc);
			}
		}

		return result;
	}

	/**
	 * Finds class with the specified name from the URL search path. Any URLs referring to JAR files are loaded and opened as needed until the class is
	 * found.
	 * 
	 * @param name
	 *          the name of the class
	 * @param bc
	 *          the target BinaryClass to write the data to
	 * @throws IOException
	 *           if failed to load class
	 * @throws IllegalArgumentException
	 *           if failed to open input stream for class
	 */
	private void loadRawClassByName(String name, BinaryClass bc) throws IOException {
		String resourceName = name.replace('.', '/').concat(".class");
		URL resource = getResource(resourceName);
		if (resource == null)
			throw new IllegalArgumentException("Can't open input stream for resource: " + name);
		try (InputStream is = resource.openStream()) {
			is.transferTo(bc.openOutputStream());
		} catch (IOException e) {
			log.error("Error while loading class data: " + name, e);
			throw e;
		}
	}

	@Override
	public byte[] getByteCode(String className) {
		BinaryClass bc = classFileManager.getCompiledClasses().get(className);
		return bc.getBytes();
	}

	@Override
	public Class<?> getDefinedClass(String name) {
		BinaryClass bc = classFileManager.getCompiledClasses().get(name);
		return bc == null ? null : bc.getDefinedClass();
	}

	@Override
	public void setDefinedClass(String name, Class<?> clazz) {
		BinaryClass bc = classFileManager.getCompiledClasses().get(name);

		if (bc == null) {
			throw new IllegalArgumentException("Attempt to set defined class for class that was not compiled?");
		}

		bc.setDefinedClass(clazz);
	}
}
