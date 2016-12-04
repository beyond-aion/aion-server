package com.aionemu.commons.configuration;

import java.lang.reflect.Field;
import java.lang.reflect.Type;

/**
 * @author SoulKeeper
 * @param <T>
 *          Type of returned value
 */
public abstract class PropertyTransformer<T> {

	/**
	 * This method transforms a value to an object instance
	 * 
	 * @param value
	 *          value that will be transformed
	 * @param field
	 *          the field the transformed value will be assigned to
	 * @param genericTypeArgs
	 *          the generic type of this object
	 * @return generated object from the value (may be null)
	 * @throws TransformationException
	 *           if something went wrong
	 */
	public final T transform(String value, Field field, Type... genericTypeArgs) throws TransformationException {
		try {
			return parseObject(value, field, genericTypeArgs);
		} catch (Exception e) {
			throw new TransformationException(
				"Error transforming \"" + value + "\" for field: " + field.getDeclaringClass().getSimpleName() + "." + field.getName(), e);
		}
	}

	protected abstract T parseObject(String value, Field field, Type... genericTypeArgs) throws Exception;
}
