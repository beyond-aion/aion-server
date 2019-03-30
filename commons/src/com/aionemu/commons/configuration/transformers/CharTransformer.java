package com.aionemu.commons.configuration.transformers;

import com.aionemu.commons.configuration.TransformationTypeInfo;

/**
 * Transforms string representation of character to character. Character may be represented only by string.
 */
public class CharTransformer extends PropertyTransformer<Character> {

	/**
	 * Shared instance of this transformer. It's thread-safe so no need of multiple instances
	 */
	public static final CharTransformer SHARED_INSTANCE = new CharTransformer();

	@Override
	protected Character parseObject(String value, TransformationTypeInfo typeInfo) {
		if (value.isEmpty())
			throw new IllegalArgumentException("Cannot convert empty string to character.");
		if (value.length() > 1)
			throw new IllegalArgumentException("Too many characters in the value.");
		return value.charAt(0);
	}
}
