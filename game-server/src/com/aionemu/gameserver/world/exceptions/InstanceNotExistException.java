package com.aionemu.gameserver.world.exceptions;

/**
 * This Exception will be thrown when some object is referencing to Instance that do not exist now.
 * 
 * @author -Nemesiss-
 */
public class InstanceNotExistException extends RuntimeException {

	private static final long serialVersionUID = -2025054197701447122L;

	/**
	 * Constructs an <code>InstanceNotExistException</code> with no detail message.
	 */
	public InstanceNotExistException() {
		super();
	}

	/**
	 * Constructs an <code>InstanceNotExistException</code> with the specified detail message.
	 * 
	 * @param s
	 *          the detail message.
	 */
	public InstanceNotExistException(String s) {
		super(s);
	}
}
