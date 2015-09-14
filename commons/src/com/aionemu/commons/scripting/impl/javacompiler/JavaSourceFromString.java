package com.aionemu.commons.scripting.impl.javacompiler;

import java.net.URI;

import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;

/**
 * This class allows us to compile sources that are located only in memory.
 * 
 * @author SoulKeeper
 */
public class JavaSourceFromString extends SimpleJavaFileObject {

	/**
	 * Source code of the class
	 */
	private final String code;

	/**
	 * Creates new object that contains sources of java class
	 * 
	 * @param className
	 *          class name of class
	 * @param code
	 *          source code of class
	 */
	public JavaSourceFromString(String className, String code) {
		super(URI.create("string:///" + className.replace('.', '/') + JavaFileObject.Kind.SOURCE.extension), JavaFileObject.Kind.SOURCE);
		this.code = code;
	}

	/**
	 * Returns class source code
	 * 
	 * @param ignoreEncodingErrors
	 *          not used
	 * @return class source code
	 */
	@Override
	public CharSequence getCharContent(boolean ignoreEncodingErrors) {
		return code;
	}
}
