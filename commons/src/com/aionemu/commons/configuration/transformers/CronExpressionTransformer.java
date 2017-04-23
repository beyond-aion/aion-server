package com.aionemu.commons.configuration.transformers;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.text.ParseException;

import org.quartz.CronExpression;

import com.aionemu.commons.configuration.Property;
import com.aionemu.commons.configuration.PropertyTransformer;

/**
 * @author Neon
 */
public class CronExpressionTransformer extends PropertyTransformer<CronExpression> {

	/**
	 * Shared instance of this transformer. It's thread-safe so no need of multiple instances
	 */
	public static final CronExpressionTransformer SHARED_INSTANCE = new CronExpressionTransformer();

	@Override
	protected CronExpression parseObject(String value, Field field, Type... genericTypeArgs) throws Exception {
		if (value.isEmpty() || value.equals(Property.DEFAULT_VALUE))
			return null;

		try {
			return new CronExpression(value);
		} catch (ParseException e) {
			throw new IllegalArgumentException("Cron expression " + value + " could not be interpreted: " + e.getMessage());
		}
	}
}
