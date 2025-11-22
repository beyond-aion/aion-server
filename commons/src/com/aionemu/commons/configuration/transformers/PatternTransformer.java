package com.aionemu.commons.configuration.transformers;

import java.util.regex.Pattern;

import com.aionemu.commons.configuration.TransformationTypeInfo;

/**
 * Automatic pattern transformer for RegExp resolving
 * 
 * @author SoulKeeper
 */
public class PatternTransformer extends PropertyTransformer<Pattern> {

	@Override
	public boolean matches(Class<?> targetType) {
		return targetType == Pattern.class;
	}

	@Override
	protected Pattern parseObject(String value, TransformationTypeInfo typeInfo) {
		return value.isEmpty() ? null : Pattern.compile(value);
	}
}
