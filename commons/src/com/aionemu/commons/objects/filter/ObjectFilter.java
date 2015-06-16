package com.aionemu.commons.objects.filter;

/**
 * Object filter interface.
 * <p/>
 * Instances of classes that implement this interface are used to filter objects.
 * <p/>
 * Created on: 31.03.2009 10:07:12
 * 
 * @author Aquanox
 * @param <T>
 */
public interface ObjectFilter<T> {

	/**
	 * Tests whether or not the specified <code>Object</code> should be included in a object list.
	 * 
	 * @param object
	 *          The object to be tested
	 * @return <code>true</code> if and only if <code>object</code> should be included
	 */
	boolean acceptObject(T object);
}
