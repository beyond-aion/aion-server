package com.aionemu.commons.database.dao;

/**
 * This class represents exception that is thrown if DAO implementation was not foud
 * 
 * @author SoulKeeper
 */
public class DAONotFoundException extends DAOException {

	/**
	 * SerialID
	 */
	private static final long serialVersionUID = 4241980426435305296L;

	public DAONotFoundException() {
	}

	/**
	 * @param message
	 */
	public DAONotFoundException(String message) {
		super(message);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public DAONotFoundException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * @param cause
	 */
	public DAONotFoundException(Throwable cause) {
		super(cause);
	}
}
