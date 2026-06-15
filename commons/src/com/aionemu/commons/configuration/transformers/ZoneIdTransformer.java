package com.aionemu.commons.configuration.transformers;

import java.time.ZoneId;

import com.aionemu.commons.configuration.TransformationTypeInfo;

/**
 * @author Neon
 */
public class ZoneIdTransformer extends PropertyTransformer<ZoneId> {

	@Override
	public boolean matches(Class<?> targetType) {
		return ZoneId.class.isAssignableFrom(targetType);
	}

	@Override
	protected ZoneId parseObject(String value, TransformationTypeInfo typeInfo) {
		return value.isEmpty() ? ZoneId.systemDefault() : ZoneId.of(value);
	}
}
