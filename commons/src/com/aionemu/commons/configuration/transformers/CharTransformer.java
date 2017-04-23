package com.aionemu.commons.configuration.transformers;

import java.lang.reflect.Field;
import java.lang.reflect.Type;

import com.aionemu.commons.configuration.PropertyTransformer;

/**
 * Transforms string representation of character to character. Character may be represented only by string.
 */
public class CharTransformer extends PropertyTransformer<Character> {

	/**
	 * Shared instance of this transformer. It's thread-safe so no need of multiple instances
	 */
	public static final CharTransformer SHARED_INSTANCE = new CharTransformer();

	/**
	 * Transforms string to character
	 * 
	 * @param value
	 *          value that will be transformed
	 * @param field
	 *          value will be assigned to this field
	 * @return Character object that represents transformed string
	 * @throws Exception
	 *           if something went wrong
	 */
	@Override
	protected Character parseObject(String value, Field field, Type... genericTypeArgs) throws Exception {
		if (value.length() > 1) {
			throw new IllegalArgumentException("Too many characters in the value");
		}
		return value.charAt(0);
	}
}
