package com.aionemu.gameserver.events;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Rolandas
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Listenable {

	/**
	 * @return true if the method should be listenable; set to false to disable temporary
	 */
	boolean value() default true;
}
