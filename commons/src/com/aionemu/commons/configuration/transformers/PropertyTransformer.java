package com.aionemu.commons.configuration.transformers;

import java.lang.reflect.Type;

import com.aionemu.commons.configuration.TransformationException;
import com.aionemu.commons.configuration.TransformationTypeInfo;

/**
 * @author SoulKeeper, Neon
 * @param <T>
 *          Type of returned value
 */
public abstract class PropertyTransformer<T> {

	/**
	 * This method transforms a value to an object instance
	 * 
	 * @param value
	 *          value that will be transformed
	 * @param type
	 *          the declared class for the value (hint to the transformer which class to instantiate - not mandatory, therefore implementation specific)
	 * @param genericTypeArgs
	 *          the generic type of the given class
	 * @return Parsed object from the value (may be null)
	 * @throws TransformationException
	 *           if something went wrong
	 */
	public final T transform(String value, Class<?> type, Type... genericTypeArgs) throws TransformationException {
		return transform(value, new TransformationTypeInfo(type, genericTypeArgs));
	}

	protected final T transform(String value, TransformationTypeInfo typeInfo) throws TransformationException {
		try {
			return parseObject(value, typeInfo);
		} catch (Exception e) {
			throw new TransformationException("Error parsing \"" + value + "\" as " + typeInfo.getType().getSimpleName(), e);
		}
	}

	protected abstract T parseObject(String value, TransformationTypeInfo typeInfo) throws Exception;
}
