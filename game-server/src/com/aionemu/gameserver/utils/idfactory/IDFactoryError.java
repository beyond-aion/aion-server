package com.aionemu.gameserver.utils.idfactory;

/**
 * This error is thrown by id factory
 * 
 * @author SoulKeeper
 */
public class IDFactoryError extends Error {

	private static final long serialVersionUID = 6945059882804355687L;

	public IDFactoryError() {

	}

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
