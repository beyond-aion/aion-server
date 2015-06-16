package com.aionemu.gameserver.restrictions;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author NB4L1
 */
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE, ElementType.METHOD })
public @interface RestrictionPriority {

	public static final double DEFAULT_PRIORITY = 0.0;

	double value() default DEFAULT_PRIORITY;
}
