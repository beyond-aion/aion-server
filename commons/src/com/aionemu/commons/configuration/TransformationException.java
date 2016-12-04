package com.aionemu.commons.configuration;

/**
 * This exception indicates errors while transforming parsed configuration values to actual class field values (according to the fields
 * {@link Property annotation}).
 * 
 * @author SoulKeeper
 * @see Property
 * @see PropertyTransformer
 * @see ConfigurableProcessor
 */
public class TransformationException extends RuntimeException {

	/**
	 * SerialID
	 */
	private static final long serialVersionUID = -6641235751743285902L;

	/**
	 * Creates new instance of exception
	 * 
	 * @param message
	 *          exception message
	 */
	public TransformationException(String message) {
		super(message);
	}

	/**
	 * Creates new instance of exception
	 * 
	 * @param message
	 *          exception message
	 * @param cause
	 *          exception that is the reason of this exception
	 */
	public TransformationException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Creates new instance of exception
	 * 
	 * @param cause
	 *          exception that is the reason of this exception
	 */
	public TransformationException(Throwable cause) {
		super(cause);
	}
}
