package com.aionemu.commons.configuration.transformers;

import org.quartz.CronExpression;

import com.aionemu.commons.configuration.TransformationTypeInfo;

/**
 * @author Neon
 */
public class CronExpressionTransformer extends PropertyTransformer<CronExpression> {

	/**
	 * Shared instance of this transformer. It's thread-safe so no need of multiple instances
	 */
	public static final CronExpressionTransformer SHARED_INSTANCE = new CronExpressionTransformer();

	@Override
	protected CronExpression parseObject(String value, TransformationTypeInfo typeInfo) throws Exception {
		return value.isEmpty() ? null : new CronExpression(value);
	}
}
