package com.aionemu.commons.configuration.transformers;

import java.lang.reflect.Field;
import java.lang.reflect.Type;

import com.aionemu.commons.configuration.PropertyTransformer;
import com.aionemu.commons.configuration.TransformationException;

/**
 * Transforms decimal that is represented as string to double
 * 
 * @author SoulKeeper
 */
public class DoubleTransformer extends PropertyTransformer<Double> {

	/**
	 * Shared instance of this transformer. It's thread-safe so no need of multiple instances
	 */
	public static final DoubleTransformer SHARED_INSTANCE = new DoubleTransformer();

	/**
	 * Transforms string to required double
	 * 
	 * @param value
	 *          value that will be transformed
	 * @param field
	 *          value will be assigned to this field
	 * @return Double that represents transformed string
	 * @throws TransformationException
	 *           if something went wrong
	 */
	@Override
	protected Double parseObject(String value, Field field, Type... genericTypeArgs) throws Exception {
		return Double.parseDouble(value);
	}
}
