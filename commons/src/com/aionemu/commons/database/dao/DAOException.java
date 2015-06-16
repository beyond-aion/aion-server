package com.aionemu.commons.database.dao;

/**
 * Generic DAO exception class
 * 
 * @author SoulKeeper
 */
public class DAOException extends RuntimeException {

	/**
	 * SerialID
	 */
	private static final long serialVersionUID = 7637014806313099318L;

	public DAOException() {
	}

	/**
	 * @param message
	 */
	public DAOException(String message) {
		super(message);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public DAOException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * @param cause
	 */
	public DAOException(Throwable cause) {
		super(cause);
	}
}
