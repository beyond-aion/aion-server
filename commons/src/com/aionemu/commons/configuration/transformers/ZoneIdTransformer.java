package com.aionemu.commons.configuration.transformers;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.time.ZoneId;

import com.aionemu.commons.configuration.Property;
import com.aionemu.commons.configuration.PropertyTransformer;

/**
 * @author Neon
 */
public class ZoneIdTransformer extends PropertyTransformer<ZoneId> {

	/**
	 * Shared instance of this transformer, it's thread safe so no need to create multiple instances
	 */
	public static final ZoneIdTransformer SHARED_INSTANCE = new ZoneIdTransformer();

	/**
	 * Transforms string to ZoneId.
	 * 
	 * @param value
	 *          Time zone id string
	 * @param field
	 *          value will be assigned to this field
	 * @return ZoneId object that represents transformed value (default or empty value returns the system default ZoneId)
	 * @throws Exception
	 *           if input string was invalid
	 */
	@Override
	protected ZoneId parseObject(String value, Field field, Type... genericTypeArgs) throws Exception {
		if (value.isEmpty() || value.equals(Property.DEFAULT_VALUE))
			return ZoneId.systemDefault();
		else
			return ZoneId.of(value);
	}
}
