// Copyright 2007 Fusionsoft, Inc. All rights reserved.
// Use is subject to license terms.
package com.aionemu.gameserver.utils.annotations;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 * The annotated method is a wrapping for some method providing inheritance of all annotations of the method being overridden by this one. If the same
 * method has different annotations in different superclasses or superinterface, the last annotation met is taken. So you better maintain the same
 * annotations in this case.
 * 
 * @author Vladimir Ovchinnikov
 * @version 1.1
 */
public interface AnnotatedMethod {

	/**
	 * @return the annotated class where the method is declared.
	 */
	AnnotatedClass getAnnotatedClass();

	/**
	 * @return the method wrapped by the annotated method.
	 */
	Method getMethod();

	/**
	 * @return all inherited and declared annotations of the method.
	 */
	Annotation[] getAllAnnotations();

	/**
	 * @param annotationClass
	 *          of the annotation to find.
	 * @return the inherited or declared annotation of the specified class.
	 */
	<T extends Annotation> T getAnnotation(Class<T> annotationClass);
}
