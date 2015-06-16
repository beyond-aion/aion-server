package com.aionemu.gameserver.world.exceptions;

/**
 * This exception will be thrown when object without set position will be spawned or despawned. This exception
 * indicating error when coder forget to set position but is spawning or despawning object.
 * 
 * @author -Nemesiss-
 */
@SuppressWarnings("serial")
public class NotSetPositionException extends RuntimeException {

	/**
	 * Constructs an <code>NotSetPositionException</code> with no detail message.
	 */
	public NotSetPositionException() {
		super();
	}

	/**
	 * Constructs an <code>NotSetPositionException</code> with the specified detail message.
	 * 
	 * @param s
	 *          the detail message.
	 */
	public NotSetPositionException(String s) {
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
	public NotSetPositionException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Creates new error
	 * 
	 * @param cause
	 *          reason of this exception
	 */
	public NotSetPositionException(Throwable cause) {
		super(cause);
	}
}
