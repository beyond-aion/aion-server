// Copyright 2007 Fusionsoft, Inc. All rights reserved.
// Use is subject to license terms.
package com.aionemu.gameserver.utils.annotations;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The core class for wrapping classes as annotated classes. The annotated class provides access to all declared and inherited annotations from
 * classes and interfaces. Also the annotated class provides wrapping for its methods for gathering all declared and inherited annotations for it from
 * base classes and interfaces.
 * <p>
 * By now only public methods can inherit annotations with the mechanism. (Comment: [RR] reworked that, now it fetches all methods, but sure it's a
 * workaround. (See: {@link com.aionemu.gameserver.utils.annotations.AnnotatedClassImpl#getAllMethods(Class, List)} 
 * 
 * @author Vladimir Ovchinnikov
 * @version 1.1
 */
public class AnnotationManager {

	private static Map<Class<?>, AnnotatedClass> classToAnnotatedMap = new ConcurrentHashMap<>();

	/**
	 * @param theClass
	 *          to wrap.
	 * @return the annotated class wrapping the specified one.
	 */
	public static AnnotatedClass getAnnotatedClass(Class<?> theClass) {
		AnnotatedClass annotatedClass = classToAnnotatedMap.get(theClass);
		if (annotatedClass == null) {
			annotatedClass = new AnnotatedClassImpl(theClass);
			classToAnnotatedMap.put(theClass, annotatedClass);
		}
		return annotatedClass;
	}

	public static boolean containsClass(Class<?> theClass) {
		return classToAnnotatedMap.get(theClass) != null;
	}
}
