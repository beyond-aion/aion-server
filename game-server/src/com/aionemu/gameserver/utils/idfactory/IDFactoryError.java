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

	public IDFactoryError(String message) {
		super(message);
	}

	public IDFactoryError(String message, Throwable cause) {
		super(message, cause);
	}

	public IDFactoryError(Throwable cause) {
		super(cause);
	}
}
