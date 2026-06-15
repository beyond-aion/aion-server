package com.aionemu.gameserver.services.cron;

import org.quartz.CronExpression;

import com.aionemu.commons.configuration.TransformationTypeInfo;
import com.aionemu.commons.configuration.transformers.PropertyTransformer;

/**
 * @author Neon
 */
public class CronExpressionTransformer extends PropertyTransformer<CronExpression> {

	@Override
	public boolean matches(Class<?> targetType) {
		return targetType == CronExpression.class;
	}

	@Override
	protected CronExpression parseObject(String value, TransformationTypeInfo typeInfo) throws Exception {
		return value.isEmpty() ? null : CronExpressions.getOrCreate(value);
	}
}
