package com.aionemu.gameserver.utils.idfactory;

/**
 * This error is thrown by id factory
 * 
 * @author SoulKeeper
 */
@SuppressWarnings("serial")
public class IDFactoryError extends Error {

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
