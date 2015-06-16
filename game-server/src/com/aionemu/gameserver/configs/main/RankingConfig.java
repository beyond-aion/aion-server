package com.aionemu.gameserver.configs.main;

import com.aionemu.commons.configuration.Property;

/**
 * @author Sarynth
 */
public class RankingConfig {

	@Property(key = "gameserver.topranking.updaterule", defaultValue = "0 0 0 * * ?")
	public static String TOP_RANKING_UPDATE_RULE;
	
	@Property(key = "gameserver.topranking.daily.gploss.time", defaultValue = "0 0 12 * * ?")
	public static String TOP_RANKING_DAILY_GP_LOSS_TIME;
	
	@Property(key = "gameserver.topranking.small.cache", defaultValue = "false")
	public static boolean TOP_RANKING_SMALL_CACHE;
	
	@Property(key = "gameserver.topranking.max.offline.days", defaultValue = "0")
	public static int TOP_RANKING_MAX_OFFLINE_DAYS;
}
