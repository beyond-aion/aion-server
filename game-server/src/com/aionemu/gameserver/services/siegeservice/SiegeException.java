package com.aionemu.gameserver.services.siegeservice;

public class SiegeException extends RuntimeException {

	private static final long serialVersionUID = 8834569185793190327L;

	public SiegeException() {
	}

	public SiegeException(String message) {
		super(message);
	}

	public SiegeException(String message, Throwable cause) {
		super(message, cause);
	}

	public SiegeException(Throwable cause) {
		super(cause);
	}
}
