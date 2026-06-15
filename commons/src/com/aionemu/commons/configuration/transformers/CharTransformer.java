package com.aionemu.commons.configuration.transformers;

import com.aionemu.commons.configuration.TransformationTypeInfo;

/**
 * Transforms string representation of character to character. Character may be represented only by string.
 */
public class CharTransformer extends PropertyTransformer<Character> {

	@Override
	public boolean matches(Class<?> targetType) {
		return targetType == char.class;
	}

	@Override
	protected Character parseObject(String value, TransformationTypeInfo typeInfo) {
		if (value.isEmpty())
			throw new IllegalArgumentException("Cannot convert empty string to character.");
		if (value.length() > 1)
			throw new IllegalArgumentException("Too many characters in the value.");
		return value.charAt(0);
	}
}
