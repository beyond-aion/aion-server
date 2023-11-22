package com.aionemu.commons.configuration.transformers;

import java.io.InvalidClassException;

import com.aionemu.commons.configuration.TransformationTypeInfo;

/**
 * Returns the {@link Class} object associated with the class or interface with the given string name. The class is not being initialized.<br>
 * Created on: 12.09.2009 15:10:47
 * 
 * @see Class#forName(String, boolean, ClassLoader)
 * @author Aquanox, Neon
 */
public class ClassTransformer extends PropertyTransformer<Class<?>> {

	public static final ClassTransformer SHARED_INSTANCE = new ClassTransformer();

	@Override
	protected Class<?> parseObject(String value, TransformationTypeInfo typeInfo) throws Exception {
		Class<?> upperBound = Object.class;
		if (typeInfo.getGenericTypeCount() > 0)
			upperBound = typeInfo.getGenericType(0).getType();
		return findClass(value, upperBound);
	}

	private Class<?> findClass(String value, Class<?> upperBound) throws ClassNotFoundException, InvalidClassException {
		if (upperBound.getSimpleName().equals(value) || upperBound.getName().equals(value)) {
			return upperBound;
		}
		Class<?> cls = Class.forName(value.contains(".") ? value : upperBound.getPackageName() + '.' + value, false, getClass().getClassLoader());
		if (!upperBound.isAssignableFrom(cls))
			throw new InvalidClassException(cls.getName() + " is not a subtype of " + upperBound.getName());
		return cls;
	}
}
