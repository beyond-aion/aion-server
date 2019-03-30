package com.aionemu.commons.configuration.transformers;

import com.aionemu.commons.configuration.TransformationTypeInfo;

/**
 * Parses most common number types.
 * 
 * @author Neon
 */
public class NumberTransformer extends PropertyTransformer<Number> {

	/**
	 * Shared instance of this transformer. It's thread-safe so no need of multiple instances
	 */
	public static final NumberTransformer SHARED_INSTANCE = new NumberTransformer();

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
		if (clazz.isPrimitive()) {
			if (clazz == Long.TYPE)
				return Long.class;
			if (clazz == Integer.TYPE)
				return Integer.class;
			if (clazz == Short.TYPE)
				return Short.class;
			if (clazz == Byte.TYPE)
				return Byte.class;
			if (clazz == Double.TYPE)
				return Double.class;
			if (clazz == Float.TYPE)
				return Float.class;
		}
		return clazz;
	}
}
