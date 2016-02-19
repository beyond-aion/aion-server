package com.aionemu.commons.configuration.transformers;

import java.lang.reflect.Field;
import java.lang.reflect.Type;

import com.aionemu.commons.configuration.PropertyTransformer;
import com.aionemu.commons.configuration.TransformationException;

/**
 * Transforms value that represents long to long. Value can be in decimal or hex format.
 */
public class LongTransformer implements PropertyTransformer<Long> {

	/**
	 * Shared instance of this transformer. It's thread-safe so no need of multiple instances
	 */
	public static final LongTransformer SHARED_INSTANCE = new LongTransformer();

	/**
	 * Transforms value to long
	 * 
	 * @param value
	 *          value that will be transformed
	 * @param field
	 *          value will be assigned to this field
	 * @return Long that represents value
	 * @throws TransformationException
	 *           if something went wrong
	 */
	@Override
	public Long transform(String value, Field field, Type... genericTypeArgs) throws TransformationException {
		try {
			return Long.decode(value);
		} catch (Exception e) {
			throw new TransformationException(e);
		}
	}
}
