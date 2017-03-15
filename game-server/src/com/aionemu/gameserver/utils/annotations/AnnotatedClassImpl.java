// Copyright 2007 Fusionsoft, Inc. All rights reserved.
// Use is subject to license terms.
package com.aionemu.gameserver.utils.annotations;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The standard implementation for the annotated class.
 * 
 * @author Vladimir Ovchinnikov
 * @version 1.1
 */
class AnnotatedClassImpl implements AnnotatedClass {

	private final Class<?> theClass;
	private Map<Class<?>, Annotation> classToAnnotationMap = null;
	private Map<Method, AnnotatedMethod> methodToAnnotatedMap = null;
	private Annotation[] annotations = null;
	private AnnotatedMethod[] annotatedMethods = null;

	AnnotatedClassImpl(Class<?> theClass) {
		super();
		this.theClass = theClass;
	}

	/**
	 * @return the cached map of classes to annotations
	 */
	private Map<Class<?>, Annotation> getAllAnnotationMap() {
		if (classToAnnotationMap == null)
			classToAnnotationMap = getAllAnnotationMapCalculated();
		return classToAnnotationMap;
	}

	/**
	 * @return the calculated map of classes to annotations
	 */
	private Map<Class<?>, Annotation> getAllAnnotationMapCalculated() {
		HashMap<Class<?>, Annotation> result = new HashMap<>();

		final Class<?> superClass = getTheClass().getSuperclass();
		// Get the superclass's annotations
		if (superClass != null)
			fillAnnotationsForOneClass(result, superClass);

		// Get the superinterfaces' annotations
		for (Class<?> c : getTheClass().getInterfaces())
			fillAnnotationsForOneClass(result, c);

		// Get its own annotations. They have preferece to inherited annotations.
		for (Annotation annotation : getTheClass().getDeclaredAnnotations())
			result.put(annotation.getClass().getInterfaces()[0], annotation);

		return result;
	}

	/**
	 * @param result
	 *          map of classes to annotations
	 * @param baseClass
	 *          is the superclass or one of the superinterfaces.
	 */
	private void fillAnnotationsForOneClass(HashMap<Class<?>, Annotation> result, Class<?> baseClass) {
		addAnnotations(result, AnnotationManager.getAnnotatedClass(baseClass).getAllAnnotations());
	}

	/**
	 * @param result
	 *          map of classes to annotations
	 * @param annotations
	 *          to add to the result
	 */
	private void addAnnotations(HashMap<Class<?>, Annotation> result, Annotation[] annotations) {
		for (Annotation annotation : annotations) {
			if (annotation == null)
				continue;
			if (result.containsKey(annotation.getClass().getInterfaces()[0]))
				result.put(annotation.getClass().getInterfaces()[0], null /* it means not to take the annotation at all */);
			else
				result.put(annotation.getClass().getInterfaces()[0], annotation);
		}
	}

	@Override
	public Class<?> getTheClass() {
		return theClass;
	}

	@Override
	public Annotation[] getAllAnnotations() {
		if (annotations == null)
			annotations = getAllAnnotationsCalculated();
		return annotations;
	}

	private Annotation[] getAllAnnotationsCalculated() {
		return getAllAnnotationMap().values().toArray(new Annotation[0]);
	}

	@Override
	public Annotation getAnnotation(Class<?> annotationClass) {
		return getAllAnnotationMap().get(annotationClass);
	}

	private Map<Method, AnnotatedMethod> getMethodMap() {
		if (methodToAnnotatedMap == null)
			methodToAnnotatedMap = getMethodMapCalculated();
		return methodToAnnotatedMap;
	}

	private Map<Method, AnnotatedMethod> getMethodMapCalculated() {
		// Preserve order of addition to map
		Map<Method, AnnotatedMethod> result = new HashMap<>();

		List<Method> methods = new ArrayList<>();
		getAllMethods(getTheClass(), methods);
		for (Method method : methods) {
			if (method.getAnnotations().length == 0)
				continue;
			result.put(method, new AnnotatedMethodImpl(this, method));
		}

		return result;
	}

	/**
	 * Gets all methods recursively [RR]
	 * 
	 * @param clazz
	 *          Class of interest
	 * @param methods
	 *          a container to hold all methods, which is filled in by call
	 */
	private void getAllMethods(Class<?> clazz, List<Method> methods) {
		if (clazz == null || clazz == Object.class)
			return;
		List<Method> declared = Arrays.asList(clazz.getDeclaredMethods());
		methods.addAll(declared);
		if (clazz.getSuperclass() != null)
			getAllMethods(clazz.getSuperclass(), methods);
	}

	@Override
	public AnnotatedMethod getAnnotatedMethod(Method method) {
		return getMethodMap().get(method);
	}

	@Override
	public AnnotatedMethod[] getAnnotatedMethods() {
		if (annotatedMethods == null)
			annotatedMethods = getAnnotatedMethodsCalculated();
		return annotatedMethods;
	}

	private AnnotatedMethod[] getAnnotatedMethodsCalculated() {
		final Collection<AnnotatedMethod> values = getMethodMap().values();
		return values.toArray(new AnnotatedMethod[values.size()]);
	}

	@Override
	public AnnotatedMethod getAnnotatedMethod(String name, Class<?>[] parameterType) {
		try {
			return getAnnotatedMethod(getTheClass().getMethod(name, parameterType));
		} catch (SecurityException e) {
			throw new RuntimeException(e);
		} catch (NoSuchMethodException e) {
			return null;
		}
	}
}
