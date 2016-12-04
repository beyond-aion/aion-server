package com.aionemu.commons.configuration.transformers;

import java.io.InvalidClassException;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.concurrent.Executors;

import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

import com.aionemu.commons.configuration.PropertyTransformer;

/**
 * Returns the {@link Class} object associated with the class or interface with the given string name. The class is not being initialized.<br>
 * Created on: 12.09.2009 15:10:47
 * 
 * @see Class#forName(String, boolean, ClassLoader)
 * @author Aquanox
 * @modified Neon
 */
public class ClassTransformer extends PropertyTransformer<Class<?>> {

	public static final ClassTransformer SHARED_INSTANCE = new ClassTransformer();
	private static Reflections rfl = null;

	@Override
	protected Class<?> parseObject(String value, Field field, Type... genericTypeArgs) throws Exception {
		Class<?> superClass = null;
		if (genericTypeArgs.length > 0) {
			if (genericTypeArgs[0] instanceof Class)
				superClass = (Class<?>) genericTypeArgs[0];
			else if (genericTypeArgs[0] instanceof ParameterizedType)
				superClass = (Class<?>) ((ParameterizedType) genericTypeArgs[0]).getRawType();
			else if (genericTypeArgs[0] instanceof WildcardType)
				superClass = (Class<?>) ((WildcardType) genericTypeArgs[0]).getUpperBounds()[0];
		}
		return findClass(value, superClass);
	}

	private Class<?> findClass(String value, Class<?> superClass) throws ClassNotFoundException, InvalidClassException {
		try {
			Class<?> cls = Class.forName(value.contains(".") ? value : "java.lang." + value, false, getClass().getClassLoader());
			if (superClass != null && !superClass.isAssignableFrom(cls))
				throw new InvalidClassException(cls.getName() + " is not an instance of " + superClass.getName());
			return cls;
		} catch (ClassNotFoundException e) {
			if (superClass != null) { // search for class via reflection: this supports simple class names without class paths
				if (rfl == null) // initialize reflections only for servers class paths, so it loads faster
					rfl = new Reflections(new ConfigurationBuilder().setUrls(ClasspathHelper.forPackage("com.aionemu", getClass().getClassLoader()))
						.setScanners(new SubTypesScanner()).setExecutorService(Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors())));
				for (Class<?> cls : rfl.getSubTypesOf(superClass)) {
					if (cls.getSimpleName().equals(value) || cls.getName().equals(value))
						return cls;
				}
			}
			throw e;
		}
	}
}
