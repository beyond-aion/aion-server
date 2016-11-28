package com.aionemu.commons.scripting.url;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import com.aionemu.commons.scripting.ScriptClassLoader;

/**
 * This class represents URL Connection that is used to "connect" to scripts binary data that was loaded by specified
 * {@link com.aionemu.commons.scripting.impl.javacompiler.ScriptCompilerImpl}.<br>
 * <br>
 * TODO: Implement all methods of {@link URLConnection} to ensure valid behaviour
 * 
 * @author SoulKeeper
 */
public class VirtualClassURLConnection extends URLConnection {

	/**
	 * Input stream, is assigned from class
	 */
	private InputStream is;

	/**
	 * Creates URL connections that "connects" to class binary data
	 * 
	 * @param url
	 *          class name
	 * @param cl
	 *          classloader
	 */
	protected VirtualClassURLConnection(URL url, ScriptClassLoader cl) {
		super(url);
		is = new ByteArrayInputStream(cl.getByteCode(url.getHost()));
	}

	/**
	 * This method is ignored
	 */
	@Override
	public void connect() throws IOException {

	}

	@Override
	public InputStream getInputStream() throws IOException {
		return is;
	}
}
