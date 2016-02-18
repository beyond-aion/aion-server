package com.aionemu.commons.configuration.transformers;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.time.DateTimeException;
import java.time.ZoneId;
import java.util.TimeZone;

import com.aionemu.commons.configuration.Property;
import com.aionemu.commons.configuration.PropertyTransformer;
import com.aionemu.commons.configuration.TransformationException;

/**
 * @author Neon
 */
public class TimeZoneTransformer implements PropertyTransformer<TimeZone> {

	/**
	 * Shared instance of this transformer, it's thread safe so no need to create multiple instances
	 */
	public static final TimeZoneTransformer SHARED_INSTANCE = new TimeZoneTransformer();

	/**
	 * Transforms string to TimeZone.
	 * 
	 * @param value
	 *          ZoneId string representation that will be transformed
	 * @param field
	 *          value will be assigned to this field
	 * @return TimeZone object that represents transformed value (default or empty value returns the system default TimeZone)
	 * @throws TransformationException
	 *           if input string was invalid
	 */
	@Override
	public TimeZone transform(String value, Field field, Type... genericTypeArgs) throws TransformationException {
		try {
			ZoneId zoneId = value.isEmpty() || value.equals(Property.DEFAULT_VALUE) ? ZoneId.systemDefault() : ZoneId.of(value);
			return TimeZone.getTimeZone(zoneId);
		} catch (DateTimeException e) {
			throw new TransformationException(
				"Invalid TimeZone ZoneId string: " + value + " for field: " + field.getDeclaringClass().getSimpleName() + "." + field.getName());
		}
	}
}
