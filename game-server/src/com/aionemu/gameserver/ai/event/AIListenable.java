package com.aionemu.gameserver.ai.event;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Rolandas
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface AIListenable {

	/**
	 * @return true if the method should be listenable; set to false to disable temporary
	 */
	boolean enabled() default true;

	AIEventType type();
}
