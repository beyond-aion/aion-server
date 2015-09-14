package com.aionemu.gameserver.network;

/**
 * This Exception will be thrown when <code>Crypt</code> setKey method will be called more than one time.
 * 
 * @author -Nemesiss-
 */
public class KeyAlreadySetException extends RuntimeException {

	private static final long serialVersionUID = 1862999213177622108L;

	/**
	 * Constructs an <code>KeyAlreadySetException</code> with no detail message.
	 */
	public KeyAlreadySetException() {
		super();
	}

	/**
	 * Constructs an <code>KeyAlreadySetException</code> with the specified detail message.
	 * 
	 * @param s
	 *          the detail message.
	 */
	public KeyAlreadySetException(String s) {
		super(s);
	}

	/**
	 * Creates new error
	 * 
	 * @param message
	 *          exception description
	 * @param cause
	 *          reason of this exception
	 */
	public KeyAlreadySetException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Creates new error
	 * 
	 * @param cause
	 *          reason of this exception
	 */
	public KeyAlreadySetException(Throwable cause) {
		super(cause);
	}
}
