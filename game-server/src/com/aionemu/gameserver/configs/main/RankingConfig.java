package com.aionemu.gameserver.configs.main;

import com.aionemu.commons.configuration.Properties;
import org.quartz.CronExpression;

import com.aionemu.commons.configuration.Property;
import com.aionemu.gameserver.utils.stats.AbyssRankEnum;

import java.util.Map;

/**
 * @author Sarynth
 */
public class RankingConfig {

	@Property(key = "gameserver.topranking.updaterule", defaultValue = "0 0 0 ? * *")
	public static CronExpression TOP_RANKING_UPDATE_RULE;

	@Property(key = "gameserver.topranking.daily.gploss.time", defaultValue = "0 0 12 ? * *")
	public static CronExpression TOP_RANKING_DAILY_GP_LOSS_TIME;

	@Property(key = "gameserver.topranking.gp_loss_cap", defaultValue = "-1")
	public static int TOP_RANKING_GP_LOSS_CAP;

	@Property(key = "gameserver.topranking.small.cache", defaultValue = "false")
	public static boolean TOP_RANKING_SMALL_CACHE;

	@Property(key = "gameserver.topranking.max.offline.days", defaultValue = "0")
	public static int TOP_RANKING_MAX_OFFLINE_DAYS;

	@Property(key = "gameserver.topranking.xform.min_rank", defaultValue = "STAR5_OFFICER")
	public static AbyssRankEnum XFORM_MIN_RANK;

	@Properties(keyPattern = "^gameserver\\.topranking\\.quota\\.(.+)")
	public static Map<AbyssRankEnum, Integer> TOP_RANKING_QUOTA;

	@Properties(keyPattern = "^gameserver\\.topranking\\.gp_loss\\.(.+)")
	public static Map<AbyssRankEnum, Integer> TOP_RANKING_GP_LOSS;
}
