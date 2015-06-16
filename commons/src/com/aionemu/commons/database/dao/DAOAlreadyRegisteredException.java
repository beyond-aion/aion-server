package com.aionemu.commons.database.dao;

/**
 * This exception is thrown if DAO is already registered in {@link com.aionemu.commons.database.dao.DAOManager}
 * 
 * @author SoulKeeper
 */
public class DAOAlreadyRegisteredException extends DAOException {

	/**
	 * SerialID
	 */
	private static final long serialVersionUID = -4966845154050833016L;

	public DAOAlreadyRegisteredException() {
	}

	/**
	 * @param message
	 */
	public DAOAlreadyRegisteredException(String message) {
		super(message);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public DAOAlreadyRegisteredException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * @param cause
	 */
	public DAOAlreadyRegisteredException(Throwable cause) {
		super(cause);
	}
}
