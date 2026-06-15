package com.aionemu.gameserver.model.base;

/**
 * @author Estrayl
 *
 */
public class BaseException extends RuntimeException {

	private static final long serialVersionUID = 4834557394251190327L;

	public BaseException() {
	}

	public BaseException(String message) {
		super(message);
	}

	public BaseException(String message, Throwable cause) {
		super(message, cause);
	}

	public BaseException(Throwable cause) {
		super(cause);
	}
}
