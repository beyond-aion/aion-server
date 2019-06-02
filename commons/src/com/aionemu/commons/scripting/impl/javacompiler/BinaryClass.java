package com.aionemu.commons.scripting.impl.javacompiler;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.net.URI;

import javax.tools.FileObject;
import javax.tools.SimpleJavaFileObject;

/**
 * This class is just a hack to make javac compiler work with classes loaded by previous classloader. Also it's used as container for loaded class
 * 
 * @author SoulKeeper
 */
public class BinaryClass extends SimpleJavaFileObject {

	/**
	 * Class data will be written here
	 */
	private final ByteArrayOutputStream baos = new ByteArrayOutputStream();

	/**
	 * The source file for this class
	 */
	private final URI sourceFile;

	/**
	 * Loaded class will be set here
	 */
	private Class<?> definedClass;

	/**
	 * Constructor that accepts class name as parameter
	 * 
	 * @param name
	 *          class name
	 */
	protected BinaryClass(String name, FileObject source) {
		super(URI.create(name), Kind.CLASS);
		this.sourceFile = source == null ? null : source.toUri();
	}

	/**
	 * Throws {@link UnsupportedOperationException}
	 * 
	 * @return nothing
	 */
	@Override
	public URI toUri() {
		return super.toUri();
	}

	/**
	 * Creates new ByteArrayInputStream, it just wraps class binary data
	 * 
	 * @return input stream for class data
	 * @throws IOException
	 *           never thrown
	 */
	@Override
	public InputStream openInputStream() throws IOException {
		return new ByteArrayInputStream(baos.toByteArray());
	}

	/**
	 * Opens ByteArrayOutputStream for class data
	 * 
	 * @return output stream
	 * @throws IOException
	 *           never thrown
	 */
	@Override
	public OutputStream openOutputStream() throws IOException {
		return baos;
	}

	/**
	 * Throws {@link UnsupportedOperationException}
	 * 
	 * @return nothing
	 */
	@Override
	public CharSequence getCharContent(boolean ignoreEncodingErrors) throws IOException {
		throw new UnsupportedOperationException();
	}

	/**
	 * Throws {@link UnsupportedOperationException}
	 * 
	 * @return nothing
	 */
	@Override
	public Writer openWriter() throws IOException {
		throw new UnsupportedOperationException();
	}

	public URI getSourceFile() {
		return sourceFile;
	}

	/**
	 * Unsupported operation, always returns 0
	 * 
	 * @return 0
	 */
	@Override
	public long getLastModified() {
		return 0;
	}

	/**
	 * Unsupported operation, returns false
	 * 
	 * @return false
	 */
	@Override
	public boolean delete() {
		return false;
	}

	/**
	 * Returns true if {@link javax.tools.JavaFileObject.Kind#CLASS}
	 * 
	 * @param simpleName
	 *          doesn't matter
	 * @param kind
	 *          kind to compare
	 * @return true if Kind is {@link javax.tools.JavaFileObject.Kind#CLASS}
	 */
	@Override
	public boolean isNameCompatible(String simpleName, Kind kind) {
		return Kind.CLASS.equals(kind);
	}

	/**
	 * Returns bytes of class
	 * 
	 * @return bytes of class
	 */
	public byte[] getBytes() {
		return baos.toByteArray();
	}

	/**
	 * Returns class that was loaded from binary data of this object
	 * 
	 * @return loaded class
	 */
	public Class<?> getDefinedClass() {
		return definedClass;
	}

	/**
	 * Sets class that was loaded by this object
	 * 
	 * @param definedClass
	 *          class that was loaded
	 */
	public void setDefinedClass(Class<?> definedClass) {
		this.definedClass = definedClass;
	}

}
