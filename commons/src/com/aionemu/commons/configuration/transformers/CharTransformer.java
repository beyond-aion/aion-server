package com.aionemu.commons.configuration.transformers;

import java.lang.reflect.Field;

import com.aionemu.commons.configuration.PropertyTransformer;
import com.aionemu.commons.configuration.TransformationException;

/**
 * Transformes string representation of character to character. Character may be represented only by string.
 */
public class CharTransformer implements PropertyTransformer<Character> {

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
	 * @throws TransformationException
	 *           if something went wrong
	 */
	@Override
	public Character transform(String value, Field field) throws TransformationException {
		try {
			char[] chars = value.toCharArray();
			if (chars.length > 1) {
				throw new TransformationException("To many characters in the value");
			}

			return chars[0];
		}
		catch (Exception e) {
			throw new TransformationException(e);
		}
	}
}
