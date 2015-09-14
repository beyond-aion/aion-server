// Copyright 2007 Fusionsoft, Inc. All rights reserved.
// Use is subject to license terms.
package com.aionemu.gameserver.utils.annotations;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 * The annotated class is a class or interface inheriting all annotations of a superclass and base interfaces. The annotation is inherited if there no
 * ambiguity arises: if the annotation is not met several times in the superclass and base interfaces.
 * 
 * @author Vladimir Ovchinnikov
 * @version 1.1
 */
public interface AnnotatedClass {

	/**
	 * @return the class which inherited annotations are calculated.
	 */
	Class<?> getTheClass();

	/**
	 * @return all the inherited or declared annotations.
	 */
	Annotation[] getAllAnnotations();

	/**
	 * @param annotationClass
	 *          to find an annotation.
	 * @return the inherited or declared annotation of the specified class.
	 */
	Annotation getAnnotation(Class<?> annotationClass);

	/**
	 * @return all the annotated methods of the class (empty if none).
	 */
	AnnotatedMethod[] getAnnotatedMethods();

	/**
	 * @param name
	 *          of the method to find.
	 * @param parameterType
	 *          of the method to find.
	 * @return the public method having the specified name and signature (null if the method is not declared in the class).
	 */
	AnnotatedMethod getAnnotatedMethod(String name, Class<?>[] parameterType);

	/**
	 * @param public method of the annotated class.
	 * @return the annotation wrapping for the method (null if the method is not declared in the class)
	 */
	AnnotatedMethod getAnnotatedMethod(Method method);
}
