package com.aionemu.commons.scripting;

import java.net.*;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.scripting.url.VirtualClassURLStreamHandler;

/**
 * Abstract class loader that should be extended by child classloaders. If needed, this class should wrap another classloader.
 *
 * @author SoulKeeper
 */
public abstract class ScriptClassLoader extends URLClassLoader {

	/**
	 * Logger
	 */
	private static final Logger log = LoggerFactory.getLogger(ScriptClassLoader.class);

	/**
	 * URL Stream handler to allow valid url generation by {@link #getResource(String)}
	 */
	@SuppressWarnings("this-escape")
	private final VirtualClassURLStreamHandler urlStreamHandler = new VirtualClassURLStreamHandler(this);

	/**
	 * Just for compatibility with {@link URLClassLoader}
	 *
	 * @param urls
	 *          list of urls
	 * @param parent
	 *          parent classloader
	 */
	public ScriptClassLoader(URL[] urls, ClassLoader parent) {
		super(urls, parent);
	}

	/**
	 * Just for compatibility with {@link URLClassLoader}
	 *
	 * @param urls
	 *          list of urls
	 */
	public ScriptClassLoader(URL[] urls) {
		super(urls);
	}

	/**
	 * Just for compatibility with {@link URLClassLoader}
	 *
	 * @param urls
	 *          list of urls
	 * @param parent
	 *          parent classloader
	 * @param factory
	 *          {@link java.net.URLStreamHandlerFactory}
	 */
	public ScriptClassLoader(URL[] urls, ClassLoader parent, URLStreamHandlerFactory factory) {
		super(urls, parent, factory);
	}

	@Override
	public URL getResource(String name) {
		if (!name.endsWith(".class")) {
			return super.getResource(name);
		}
		String newName = name.substring(0, name.length() - 6);
		newName = newName.replace('/', '.');
		if (getCompiledClasses().contains(newName)) {
			try {
				return URL.of(URI.create(VirtualClassURLStreamHandler.HANDLER_PROTOCOL + newName), urlStreamHandler);
			} catch (MalformedURLException e) {
				log.error("Can't create url for compiled class", e);
			}
		}

		return super.getResource(name);
	}

	/**
	 * Loads class from library, parent or compiled
	 *
	 * @param name
	 *          class to load
	 * @return loaded class
	 * @throws ClassNotFoundException
	 *           if class not found
	 */
	@Override
	public Class<?> loadClass(String name) throws ClassNotFoundException {
		boolean isCompiled = getCompiledClasses().contains(name);
		if (!isCompiled) {
			return super.loadClass(name, true);
		}

		Class<?> c = getDefinedClass(name);
		if (c == null) {
			byte[] b = getByteCode(name);
			c = super.defineClass(name, b, 0, b.length);
			setDefinedClass(name, c);
		}
		return c;
	}

	/**
	 * Retuns unmodifiable set of class names that were compiled
	 *
	 * @return unmodifiable set of class names that were compiled
	 */
	public abstract Set<String> getCompiledClasses();

	/**
	 * Returns bytecode for given className. Array is copy of actual bytecode, so modifications will not harm.
	 *
	 * @param className
	 *          class name
	 * @return bytecode
	 */
	public abstract byte[] getByteCode(String className);

	/**
	 * Returns cached class instance for give name or null if is not cached yet
	 *
	 * @param name
	 *          class name
	 * @return cached class instance or null
	 */
	public abstract Class<?> getDefinedClass(String name);

	/**
	 * Sets defined class into cache
	 *
	 * @param name
	 *          class name
	 * @param clazz
	 *          class object
	 * @throws IllegalArgumentException
	 *           if class was not loaded by this class loader
	 */
	public abstract void setDefinedClass(String name, Class<?> clazz) throws IllegalArgumentException;
}
