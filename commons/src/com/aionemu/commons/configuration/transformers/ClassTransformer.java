package com.aionemu.commons.configuration.transformers;

import java.lang.reflect.Field;

import com.aionemu.commons.configuration.PropertyTransformer;
import com.aionemu.commons.configuration.TransformationException;

/**
 * Returns the <code>Class</code> object associated with the class or interface with the given string name. The class is not being initialized. <br />
 * Created on: 12.09.2009 15:10:47
 * 
 * @see Class#forName(String)
 * @see Class#forName(String, boolean, ClassLoader)
 * @author Aquanox
 */
public class ClassTransformer implements PropertyTransformer<Class<?>> {

	/** Shared instance. */
	public static final ClassTransformer SHARED_INSTANCE = new ClassTransformer();

	@Override
	public Class<?> transform(String value, Field field) throws TransformationException {
		try {
			return Class.forName(value, false, getClass().getClassLoader());
		} catch (ClassNotFoundException e) {
			throw new TransformationException("Cannot find class with name '" + value + "'");
		}
	}
}
