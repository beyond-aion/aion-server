package com.aionemu.commons.configuration.transformers;

import java.util.Map;

import com.aionemu.commons.configuration.TransformationTypeInfo;

/**
 * Parses most common number types.
 * 
 * @author Neon
 */
public class NumberTransformer extends PropertyTransformer<Number> {

	// @formatter:off
	private static final Map<Class<?>, Class<?>> primitiveWrappers = Map.of(
		byte.class, Byte.class,
		short.class, Short.class,
		int.class, Integer.class,
		long.class, Long.class,
		float.class, Float.class,
		double.class, Double.class
	);
	// @formatter:on

	@Override
	public boolean matches(Class<?> targetType) {
		return Number.class.isAssignableFrom(toWrapper(targetType));
	}

	@Override
	protected Number parseObject(String value, TransformationTypeInfo typeInfo) {
		Class<?> cls = toWrapper(typeInfo.getType());
		if (cls == Long.class)
			return Long.decode(value);
		if (cls == Integer.class)
			return Integer.decode(value);
		if (cls == Short.class)
			return Short.decode(value);
		if (cls == Byte.class)
			return Byte.decode(value);
		if (cls == Double.class)
			return Double.valueOf(value);
		if (cls == Float.class)
			return Float.valueOf(value);
		throw new UnsupportedOperationException("Number of type " + typeInfo.getType().getSimpleName() + " is not supported.");
	}

	public static Class<?> toWrapper(Class<?> clazz) {
		return clazz.isPrimitive() ? primitiveWrappers.getOrDefault(clazz, clazz) : clazz;
	}
}
