package com.aionemu.commons.configuration.transformers;

import java.lang.reflect.Field;
import java.lang.reflect.Type;

import com.aionemu.commons.configuration.PropertyTransformer;
import com.aionemu.commons.configuration.TransformationException;

/**
 * Transforms string that represents float in decimal format
 * 
 * @author SoulKeeper
 */
public class FloatTransformer extends PropertyTransformer<Float> {

	/**
	 * Shared instance of this transformer. It's thread-safe so no need of multiple instances
	 */
	public static final FloatTransformer SHARED_INSTANCE = new FloatTransformer();

	/**
	 * Transforms string to float
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
	protected Float parseObject(String value, Field field, Type... genericTypeArgs) throws Exception {
		return Float.parseFloat(value);
	}
}
