package com.aionemu.commons.configuration.transformers;

import java.lang.reflect.Field;

import com.aionemu.commons.configuration.PropertyTransformer;
import com.aionemu.commons.configuration.TransformationException;

/**
 * Thransforms string that represents float in decimal format
 * 
 * @author SoulKeeper
 */
public class FloatTransformer implements PropertyTransformer<Float> {

	/**
	 * Shared instance of this transformer. It's thread-safe so no need of multiple instances
	 */
	public static final FloatTransformer SHARED_INSTANCE = new FloatTransformer();

	/**
	 * Thransforms string to float
	 * 
	 * @param value
	 *          value that will be transformed
	 * @param field
	 *          value will be assigned to this field
	 * @return Float that represents value
	 * @throws TransformationException
	 *           if something went wrong
	 */
	@Override
	public Float transform(String value, Field field) throws TransformationException {
		try {
			return Float.parseFloat(value);
		} catch (Exception e) {
			throw new TransformationException(e);
		}
	}
}
