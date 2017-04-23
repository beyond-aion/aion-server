package com.aionemu.commons.configuration.transformers;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.TimeZone;

import com.aionemu.commons.configuration.Property;
import com.aionemu.commons.configuration.PropertyTransformer;

/**
 * @author Neon
 */
public class TimeZoneTransformer extends PropertyTransformer<TimeZone> {

	/**
	 * Shared instance of this transformer, it's thread safe so no need to create multiple instances
	 */
	public static final TimeZoneTransformer SHARED_INSTANCE = new TimeZoneTransformer();

	/**
	 * Transforms string to TimeZone.
	 * 
	 * @param value
	 *          Time zone id string
	 * @param field
	 *          value will be assigned to this field
	 * @return TimeZone object that represents transformed value (default or empty value returns the system default TimeZone)
	 * @throws Exception
	 *           if input string was invalid
	 */
	@Override
	protected TimeZone parseObject(String value, Field field, Type... genericTypeArgs) throws Exception {
		if (value.isEmpty() || value.equals(Property.DEFAULT_VALUE))
			return TimeZone.getDefault();
		else
			return TimeZone.getTimeZone(value);
	}
}
