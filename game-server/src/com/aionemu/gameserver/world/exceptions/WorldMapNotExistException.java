package com.aionemu.gameserver.world.exceptions;

/**
 * This Exception will be thrown when some object is referencing to World map that do not exist. This Exception
 * indicating serious error.
 * 
 * @author -Nemesiss-
 */
@SuppressWarnings("serial")
public class WorldMapNotExistException extends RuntimeException {

	/**
	 * Constructs an <code>WorldMapNotExistException</code> with no detail message.
	 */
	public WorldMapNotExistException() {
		super();
	}

	/**
	 * Constructs an <code>WorldMapNotExistException</code> with the specified detail message.
	 * 
	 * @param s
	 *          the detail message.
	 */
	public WorldMapNotExistException(String s) {
		super(s);
	}
}
