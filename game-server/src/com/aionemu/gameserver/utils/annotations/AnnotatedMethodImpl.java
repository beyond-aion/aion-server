// Copyright 2007 Fusionsoft, Inc. All rights reserved.
// Use is subject to license terms.
package com.aionemu.gameserver.utils.annotations;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

class AnnotatedMethodImpl implements AnnotatedMethod {

	private final AnnotatedClass annotatedClass;
	private final Method method;
	private Map<Class<?>, Annotation> classToAnnotationMap = null;
	private Annotation[] annotations = null;

	AnnotatedMethodImpl(final AnnotatedClass annotatedClass, final Method method) {
		super();
		this.annotatedClass = annotatedClass;
		this.method = method;
	}

	private Map<Class<?>, Annotation> getAllAnnotationMap() {
		if (classToAnnotationMap == null)
			classToAnnotationMap = getAllAnnotationMapCalculated();
		return classToAnnotationMap;
	}

	private Map<Class<?>, Annotation> getAllAnnotationMapCalculated() {
		HashMap<Class<?>, Annotation> result = new HashMap<>();

		final Class<?> superClass = getAnnotatedClass().getTheClass().getSuperclass();
		// Get the superclass's overridden method annotations
		if (superClass != null)
			fillAnnotationsForOneMethod(result,
				AnnotationManager.getAnnotatedClass(superClass).getAnnotatedMethod(getMethod().getName(), getMethod().getParameterTypes()));

		// Get the superinterfaces' overridden method annotations
		for (Class<?> c : getAnnotatedClass().getTheClass().getInterfaces()) {
			fillAnnotationsForOneMethod(result,
				AnnotationManager.getAnnotatedClass(c).getAnnotatedMethod(getMethod().getName(), getMethod().getParameterTypes()));
		}

		// Get its own annotations. They have preferece to inherited annotations.
		for (Annotation annotation : getMethod().getDeclaredAnnotations())
			result.put(annotation.getClass().getInterfaces()[0], annotation);

		return result;
	}

	/**
	 * @param result
	 *          is the map of classes to annotations to fill
	 * @param annotatedMethod
	 *          the method to get annotations. Does nothing if the annotated method is null.
	 */
	private void fillAnnotationsForOneMethod(final HashMap<Class<?>, Annotation> result, final AnnotatedMethod annotatedMethod) {
		if (annotatedMethod == null)
			return;
		addAnnotations(result, annotatedMethod.getAllAnnotations());
	}

	/**
	 * @param result
	 *          map of classes to annotations
	 * @param annotations
	 *          to add to the result
	 */
	private void addAnnotations(final HashMap<Class<?>, Annotation> result, final Annotation[] annotations) {
		for (Annotation annotation : annotations) {
			if (annotation == null)
				continue;
			result.put(annotation.getClass().getInterfaces()[0], annotation); /* It means to take the last annotation */
			// if (result.containsKey(annotation.getClass().getInterfaces()[0]))
			// result.put(annotation.getClass().getInterfaces()[0],
			// null /*it means not to take the annotation at all*/);
			// else
			// result.put(annotation.getClass().getInterfaces()[0], annotation);
		}
	}

	@Override
	public Annotation[] getAllAnnotations() {
		if (annotations == null)
			annotations = getAllAnnotationsCalculated();
		return annotations;
	}

	private Annotation[] getAllAnnotationsCalculated() {
		final Collection<Annotation> values = getAllAnnotationMap().values();
		return values.toArray(new Annotation[0]);
	}

	@Override
	public AnnotatedClass getAnnotatedClass() {
		return annotatedClass;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
		return (T) getAllAnnotationMap().get(annotationClass);
	}

	@Override
	public Method getMethod() {
		return method;
	}

}
