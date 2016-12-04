package com.aionemu.commons.configuration.transformers;

import java.lang.reflect.Field;
import java.lang.reflect.Type;

import com.aionemu.commons.configuration.PropertyTransformer;
import com.aionemu.commons.configuration.TransformationException;

/**
 * Transforms string that represents short to the short value. Short value can be represented as decimal or hex
 * 
 * @author SoulKeeper
 */
public class ShortTransformer extends PropertyTransformer<Short> {

	/**
	 * Shared instance of this transformer. It's thread-safe so no need of multiple instances
	 */
	public static final ShortTransformer SHARED_INSTANCE = new ShortTransformer();

	/**
	 * Transforms value to short
	 * 
	 * @param value
	 *          value that will be transformed
	 * @param field
	 *          value will be assigned to this field
	 * @return Short object that represents value
	 * @throws TransformationException
	 *           if something went wrong
	 */
	@Override
	protected Short parseObject(String value, Field field, Type... genericTypeArgs) throws Exception {
		return Short.decode(value);
	}
}
