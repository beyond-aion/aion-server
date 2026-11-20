package com.aionemu.gameserver.configs.main;

import org.quartz.CronExpression;

import com.aionemu.commons.configuration.Property;

/**
 * @author Sarynth
 */
public class RankingConfig {

	@Property(key = "gameserver.topranking.updaterule", defaultValue = "0 0 12 ? * *")
	public static CronExpression TOP_RANKING_UPDATE_RULE;

	@Property(key = "gameserver.topranking.legion_limit", defaultValue = "50")
	public static int RANKING_LIST_LEGION_LIMIT;

	@Property(key = "gameserver.topranking.max.offline.days", defaultValue = "30")
	public static int TOP_RANKING_MAX_OFFLINE_DAYS;
}
