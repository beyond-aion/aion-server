package com.aionemu.gameserver.services.cron;

import java.text.ParseException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.quartz.CronExpression;

public class CronExpressions {

	private static final Map<String, CronExpression> cronExpressions = new ConcurrentHashMap<>();

	private CronExpressions() {
	}

	public static CronExpression getOrCreate(String cronExpression) {
		return cronExpressions.computeIfAbsent(cronExpression, _ -> {
			try {
				return new CronExpression(cronExpression);
			} catch (ParseException e) {
				throw new RuntimeException(e);
			}
		});
	}
}
