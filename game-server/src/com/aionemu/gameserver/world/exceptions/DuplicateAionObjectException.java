package com.aionemu.gameserver.world.exceptions;

/**
 * This Exception will be thrown when some AionObject will be stored more then one time. This Exception indicating
 * serious error.
 * 
 * @author -Nemesiss-
 */
public class DuplicateAionObjectException extends RuntimeException {

	private static final long serialVersionUID = -2031489557355197834L;

	/**
	 * Constructs an <code>DuplicateAionObjectException</code> with no detail message.
	 */
	public DuplicateAionObjectException() {
		super();
	}

	/**
	 * Constructs an <code>DuplicateAionObjectException</code> with the specified detail message.
	 * 
	 * @param s
	 *          the detail message.
	 */
	public DuplicateAionObjectException(String s) {
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
	public DuplicateAionObjectException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Creates new error
	 * 
	 * @param cause
	 *          reason of this exception
	 */
	public DuplicateAionObjectException(Throwable cause) {
		super(cause);
	}
}
