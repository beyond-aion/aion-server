package com.aionemu.commons.configuration;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.aionemu.commons.configuration.transformers.ArrayTransformer;
import com.aionemu.commons.configuration.transformers.BooleanTransformer;
import com.aionemu.commons.configuration.transformers.ByteTransformer;
import com.aionemu.commons.configuration.transformers.CharTransformer;
import com.aionemu.commons.configuration.transformers.ClassTransformer;
import com.aionemu.commons.configuration.transformers.CollectionTransformer;
import com.aionemu.commons.configuration.transformers.DoubleTransformer;
import com.aionemu.commons.configuration.transformers.EnumTransformer;
import com.aionemu.commons.configuration.transformers.FileTransformer;
import com.aionemu.commons.configuration.transformers.FloatTransformer;
import com.aionemu.commons.configuration.transformers.InetSocketAddressTransformer;
import com.aionemu.commons.configuration.transformers.IntegerTransformer;
import com.aionemu.commons.configuration.transformers.LongTransformer;
import com.aionemu.commons.configuration.transformers.PatternTransformer;
import com.aionemu.commons.configuration.transformers.ShortTransformer;
import com.aionemu.commons.configuration.transformers.StringTransformer;
import com.aionemu.commons.configuration.transformers.TimeZoneTransformer;

/**
 * This annotation is used to mark fields that should be processed by {@link com.aionemu.commons.configuration.ConfigurableProcessor}
 * <p/>
 * List of supported types:<br>
 * <ul>
 * <li>{@link Boolean} and boolean by {@link BooleanTransformer}</li>
 * <li>{@link Byte} and byte by {@link ByteTransformer}</li>
 * <li>{@link Character} and char by {@link CharTransformer}</li>
 * <li>{@link Short} and short by {@link ShortTransformer}</li>
 * <li>{@link Integer} and int by {@link IntegerTransformer}</li>
 * <li>{@link Float} and float by {@link FloatTransformer}</li>
 * <li>{@link Long} and long by {@link LongTransformer}</li>
 * <li>{@link Double} and double by {@link DoubleTransformer}</li>
 * <li>{@link String} by {@link StringTransformer}</li>
 * <li>{@link Class} by {@link ClassTransformer}</li>
 * <li>{@link Enum} and enum by {@link EnumTransformer}</li>
 * <li>{@link java.util.Collection} types by {@link CollectionTransformer} (supports entries of the types in this list, except nested Collections)</li>
 * <li>{@code Object Arrays} by {@link ArrayTransformer} (supports entries of the types in this list, but no multi-dimensional arrays)</li>
 * <li>{@link java.io.File} by {@link FileTransformer}</li>
 * <li>{@link java.net.InetSocketAddress} by {@link InetSocketAddressTransformer}</li>
 * <li>{@link java.util.regex.Pattern} by {@link PatternTransformer}
 * <li>{@link java.util.TimeZone} by {@link TimeZoneTransformer}</li>
 * </ul>
 */
@Documented
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Property {

	/**
	 * This string shows to {@link com.aionemu.commons.configuration.ConfigurableProcessor} that init value of the object should not be overridden.
	 */
	public static final String DEFAULT_VALUE = "DO_NOT_OVERWRITE_INITIALIAZION_VALUE";

	/**
	 * Property name in configuration
	 * 
	 * @return name of the property that will be used
	 */
	public String key();

	/**
	 * Represents default value that will be parsed if key not found. If this key equals(default) {@link #DEFAULT_VALUE} init value of the object won't
	 * be overriden
	 * 
	 * @return default value of the property
	 */
	public String defaultValue() default DEFAULT_VALUE;
}
