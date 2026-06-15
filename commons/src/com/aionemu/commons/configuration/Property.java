package com.aionemu.commons.configuration;

import java.lang.annotation.*;

import com.aionemu.commons.configuration.transformers.PropertyTransformers;

/**
 * This annotation is used to mark fields that should be processed by {@link com.aionemu.commons.configuration.ConfigurableProcessor}
 * <p/>
 * Supported field types are controlled by {@link PropertyTransformers}.
 */
@Documented
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Property {

	/**
	 * This string shows to {@link com.aionemu.commons.configuration.ConfigurableProcessor} that init value of the object should not be overridden.
	 */
	String DEFAULT_VALUE = "DO_NOT_OVERWRITE_INITIALIAZION_VALUE";

	/**
	 * Property name in configuration
	 * 
	 * @return name of the property that will be used
	 */
	String key();

	/**
	 * Represents the default value that will be parsed if key was not found.<br>
	 * If the default value is not specified, it holds the special value {@link #DEFAULT_VALUE}, in which case the init value of the annotated field
	 * won't be overridden
	 * 
	 * @return default value of the property
	 */
	String defaultValue() default DEFAULT_VALUE;
}
