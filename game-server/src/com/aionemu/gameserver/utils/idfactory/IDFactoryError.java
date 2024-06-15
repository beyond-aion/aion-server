package com.aionemu.gameserver.utils.idfactory;

/**
 * Exception thrown by IDFactory to indicate errors related to ID generation and management.
 * This is a subclass of Error, which indicates serious problems that a reasonable application
 * should not try to catch.
 *
 * Typically, this exception is thrown when an illegal or unexpected operation is attempted
 * in the IDFactory class.
 *
 * @author SoulKeeper
 */
public class IDFactoryError extends Error {

	private static final long serialVersionUID = 6945059882804355687L;

	/**
	 * Constructs a new IDFactoryError with the specified detail message.
	 *
	 * @param message the detail message (which is saved for later retrieval by the getMessage() method)
	 */
	public IDFactoryError(String message) {
		super(message);
	}

	/**
	 * Constructs a new IDFactoryError with the specified detail message and cause.
	 *
	 * @param message the detail message (which is saved for later retrieval by the getMessage() method)
	 * @param cause the cause (which is saved for later retrieval by the getCause() method)
	 */
	public IDFactoryError(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructs a new IDFactoryError with the specified cause.
	 *
	 * @param cause the cause (which is saved for later retrieval by the getCause() method)
	 */
	public IDFactoryError(Throwable cause) {
		super(cause);
	}
}
