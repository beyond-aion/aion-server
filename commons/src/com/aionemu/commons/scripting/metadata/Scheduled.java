package com.aionemu.commons.scripting.metadata;

import java.lang.annotation.*;

/**
 * Annotation that can be applied on Runnable classes in scripts.<br>
 * All the scheduled tasks will be automatically cancelled in case of unloading script context<br>
 * Please note that loading/unloading is controlled by {@link com.aionemu.commons.scripting.classlistener.ScheduledTaskClassListener}
 *
 * @author SoulKeeper
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Scheduled {

	/**
	 * Array of cron expressions. Each one should be valid.
	 *
	 * @return Array of cron expressions that will be used to schedule runnable
	 */
	String[] value();

	/**
	 * Effective only in case if {@link #value()} has more than 1 element.<br>
	 * If true - a new instance of the runnable will be created for each cron expression.<br>
	 * If false - single instance will be triggered multiple times
	 */
	boolean instancePerCronExpression() default false;

	/**
	 * If this scheduler should be disabled ignored
	 * @return disabled or not
	 */
	boolean disabled() default false;

	/**
	 * Indicates if this task is long-running task or not.<br>
	 * @return true if is long-running task
	 */
	boolean longRunningTask() default false;
}
