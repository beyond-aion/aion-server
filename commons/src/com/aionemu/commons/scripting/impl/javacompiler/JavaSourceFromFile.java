package com.aionemu.commons.scripting.impl.javacompiler;

import java.io.File;
import java.io.IOException;

import javax.tools.SimpleJavaFileObject;

import org.apache.commons.io.FileUtils;

/**
 * This class is simple wrapper for SimpleJavaFileObject that load class source from file sytem
 * 
 * @author SoulKeeper
 */
public class JavaSourceFromFile extends SimpleJavaFileObject {

	/**
	 * Construct a JavaFileObject of the given kind and with the given File.
	 * 
	 * @param file
	 *          the file with source of this file object
	 * @param kind
	 *          the kind of this file object
	 */
	public JavaSourceFromFile(File file, Kind kind) {
		super(file.toURI(), kind);
	}

	/**
	 * Returns class source represented as string.
	 * 
	 * @param ignoreEncodingErrors
	 *          not used
	 * @return class source
	 * @throws IOException
	 *           if something goes wrong
	 */
	@Override
	public CharSequence getCharContent(boolean ignoreEncodingErrors) throws IOException {
		return FileUtils.readFileToString(new File(this.toUri()));
	}
}
