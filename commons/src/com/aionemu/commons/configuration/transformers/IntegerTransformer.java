package com.aionemu.commons.configuration.transformers;

import java.lang.reflect.Field;
import java.lang.reflect.Type;

import com.aionemu.commons.configuration.PropertyTransformer;
import com.aionemu.commons.configuration.TransformationException;

/**
 * Transforms string to integer. Integer can be represented both as decimal or hex value.
 * 
 * @author SoulKeeper
 */
public class IntegerTransformer extends PropertyTransformer<Integer> {

	/**
	 * Shared instance of this transformer. It's thread-safe so no need of multiple instances
	 */
	public static final IntegerTransformer SHARED_INSTANCE = new IntegerTransformer();

	/**
	 * Transforms value to integer
	 * 
	 * @param value
	 *          value that will be transformed
	 * @param field
	 *          value will be assigned to this field
	 * @return Integer object that represents value
	 * @throws TransformationException
	 *           if something went wrong
	 */
	@Override
	protected Integer parseObject(String value, Field field, Type... genericTypeArgs) throws Exception {
		return Integer.decode(value);
	}
}
