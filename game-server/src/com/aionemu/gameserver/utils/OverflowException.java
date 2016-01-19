package com.aionemu.gameserver.utils;

/**
 * @author MrPoke
 */
public class OverflowException extends Error {

	/**
	 * 
	 */
	private static final long serialVersionUID = 488570750616236378L;

	public OverflowException(String message) {
		super(message);
	}
}
