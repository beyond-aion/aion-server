package com.aionemu.commons.configuration.transformers;

import java.util.TimeZone;

import com.aionemu.commons.configuration.TransformationTypeInfo;

/**
 * @author Neon
 */
public class TimeZoneTransformer extends PropertyTransformer<TimeZone> {

	/**
	 * Shared instance of this transformer, it's thread safe so no need to create multiple instances
	 */
	public static final TimeZoneTransformer SHARED_INSTANCE = new TimeZoneTransformer();

	@Override
	protected TimeZone parseObject(String value, TransformationTypeInfo typeInfo) {
		return value.isEmpty() ? TimeZone.getDefault() : TimeZone.getTimeZone(value);
	}
}
