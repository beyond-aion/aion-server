package com.aionemu.commons.configuration.transformers;

import com.aionemu.commons.configuration.TransformationTypeInfo;

/**
 * Transforms enum string representation to enum. String must match case definition of enum, for instance:
 * 
 * <pre>
 * enum{
 *  FILE,
 *  URL
 * }
 * </pre>
 * 
 * will be parsed with string "FILE" but not "file".
 * 
 * @author SoulKeeper
 */
public class EnumTransformer extends PropertyTransformer<Enum<?>> {

	@Override
	public boolean matches(Class<?> targetType) {
		return targetType.isEnum();
	}

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected Enum<?> parseObject(String value, TransformationTypeInfo typeInfo) {
		return value.isEmpty() ? null : Enum.valueOf((Class<? extends Enum>) typeInfo.getType(), value);
	}
}
