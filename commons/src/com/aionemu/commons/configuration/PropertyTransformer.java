package com.aionemu.commons.configuration;

import java.lang.reflect.Field;

/**
 * This insterface represents property transformer, each transformer should implement it.
 * 
 * @author SoulKeeper
 * @param <T>
 *          Type of returned value
 */
public interface PropertyTransformer<T> {

	/**
	 * This method actually transforms value to object instance
	 * 
	 * @param value
	 *          value that will be transformed
	 * @param field
	 *          value will be assigned to this field
	 * @return result of transformation
	 * @throws TransformationException
	 *           if something went wrong
	 */
	public T transform(String value, Field field) throws TransformationException;
}
