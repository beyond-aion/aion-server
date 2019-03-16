package com.aionemu.commons.scripting.impl.javacompiler;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import javax.tools.SimpleJavaFileObject;

/**
 * This class is a simple wrapper for SimpleJavaFileObject that loads a class source from file system
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
		return new String(Files.readAllBytes(Paths.get(toUri())), StandardCharsets.UTF_8);
	}
}
