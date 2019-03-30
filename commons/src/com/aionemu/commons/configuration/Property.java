package com.aionemu.commons.configuration;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.aionemu.commons.configuration.transformers.ArrayTransformer;
import com.aionemu.commons.configuration.transformers.BooleanTransformer;
import com.aionemu.commons.configuration.transformers.CharTransformer;
import com.aionemu.commons.configuration.transformers.ClassTransformer;
import com.aionemu.commons.configuration.transformers.CollectionTransformer;
import com.aionemu.commons.configuration.transformers.CronExpressionTransformer;
import com.aionemu.commons.configuration.transformers.EnumTransformer;
import com.aionemu.commons.configuration.transformers.FileTransformer;
import com.aionemu.commons.configuration.transformers.InetSocketAddressTransformer;
import com.aionemu.commons.configuration.transformers.NumberTransformer;
import com.aionemu.commons.configuration.transformers.PatternTransformer;
import com.aionemu.commons.configuration.transformers.StringTransformer;
import com.aionemu.commons.configuration.transformers.TimeZoneTransformer;
import com.aionemu.commons.configuration.transformers.ZoneIdTransformer;

/**
 * This annotation is used to mark fields that should be processed by {@link com.aionemu.commons.configuration.ConfigurableProcessor}
 * <p/>
 * List of supported types:<br>
 * <ul>
 * <li>{@link Number Numbers} and their primitive counterparts by {@link NumberTransformer}</li>
 * <li>{@link Boolean} and boolean by {@link BooleanTransformer}</li>
 * <li>{@link Character} and char by {@link CharTransformer}</li>
 * <li>{@link String} by {@link StringTransformer}</li>
 * <li>{@link Class} by {@link ClassTransformer}</li>
 * <li>{@link Enum} by {@link EnumTransformer}</li>
 * <li>{@link java.util.Collection} types by {@link CollectionTransformer} (supports elements of the types in this list)</li>
 * <li>{@code Object Arrays} by {@link ArrayTransformer} (supports elements of the types in this list)</li>
 * <li>{@link org.quartz.CronExpression} by {@link CronExpressionTransformer}</li>
 * <li>{@link java.io.File} by {@link FileTransformer}</li>
 * <li>{@link java.net.InetSocketAddress} by {@link InetSocketAddressTransformer}</li>
 * <li>{@link java.util.regex.Pattern} by {@link PatternTransformer}
 * <li>{@link java.util.TimeZone} by {@link TimeZoneTransformer}</li>
 * <li>{@link java.time.ZoneId} by {@link ZoneIdTransformer}</li>
 * </ul>
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
