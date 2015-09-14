package com.aionemu.commons.callbacks.metadata;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.aionemu.commons.callbacks.Callback;

/**
 * Annotation that is used to mark enhanceable methods or classes.<br>
 * <b>Abstract and native methods are not allowed</b>
 *
 * @author SoulKeeper
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@SuppressWarnings("rawtypes")
public @interface GlobalCallback {

	/**
	 * Returns callback class that will be used as listener
	 *
	 * @return callback class that will be used as listener
	 */
	Class<? extends Callback> value();
}
