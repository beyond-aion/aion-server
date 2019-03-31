package com.aionemu.commons.configuration;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This is a specialized form of the {@link Property} annotation. It's used to generate a key-value map of select config keys.
 * 
 * @author Neon
 */
@Documented
@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface Properties {

	/**
	 * Pattern that is used to filter which properties should end up in the annotated result map. If the pattern contains a capturing group, the
	 * group content will be the map keys. Otherwise the whole matched property keys will be the map keys.
	 * Example:
	 * <ul>
	 *   <li>{@code keyPattern = "^some\\.property\\..+"} - All properties starting with "some.property." will be in the map.</li>
	 *   <li>{@code keyPattern = "^some\\.property\\.(.+)"} - All properties starting with "some.property." will be in the map, but keys of the result 
	 *   map will not start with "some.property." anymore.</li>
	 * </ul>
	 */
	String keyPattern() default ".+";
}
