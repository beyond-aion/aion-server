package com.aionemu.gameserver.configs.main;

import java.util.Set;

import org.quartz.CronExpression;

import com.aionemu.commons.configuration.Property;

public class CustomConfig {

	/**
	 * Enables challenge tasks
	 */
	@Property(key = "gameserver.challenge.tasks.enabled", defaultValue = "false")
	public static boolean CHALLENGE_TASKS_ENABLED;

	/**
	 * Announce when a player successfully enchants an item to +15 or +20
	 */
	@Property(key = "gameserver.enchant.announce.enable", defaultValue = "true")
	public static boolean ENABLE_ENCHANT_ANNOUNCE;

	/**
	 * Enable speaking between factions
	 */
	@Property(key = "gameserver.chat.factions.enable", defaultValue = "false")
	public static boolean SPEAKING_BETWEEN_FACTIONS;

	/**
	 * Minimum level to use whisper
	 */
	@Property(key = "gameserver.chat.whisper.level", defaultValue = "10")
	public static int LEVEL_TO_WHISPER;

	/**
	 * Time in days after which an item in broker will be unregistered (client cannot display more than 255 days)
	 */
	@Property(key = "gameserver.broker.registration_expiration_days", defaultValue = "8")
	public static int BROKER_REGISTRATION_EXPIRATION_DAYS;

	/**
	 * Factions search mode
	 */
	@Property(key = "gameserver.search.factions.mode", defaultValue = "false")
	public static boolean FACTIONS_SEARCH_MODE;

	/**
	 * list gm when search players
	 */
	@Property(key = "gameserver.search.gm.list", defaultValue = "false")
	public static boolean SEARCH_GM_LIST;

	/**
	 * Minimum level to use search
	 */
	@Property(key = "gameserver.search.player.level", defaultValue = "10")
	public static int LEVEL_TO_SEARCH;

	/**
	 * Allow opposite factions to bind in enemy territories
	 */
	@Property(key = "gameserver.cross.faction.binding", defaultValue = "false")
	public static boolean ENABLE_CROSS_FACTION_BINDING;

	/**
	 * Enable second class change without quest
	 */
	@Property(key = "gameserver.simple.secondclass.enable", defaultValue = "false")
	public static boolean ENABLE_SIMPLE_2NDCLASS;

	/**
	 * Disable chain trigger rate (chain skill with 100% success)
	 */
	@Property(key = "gameserver.skill.chain.disable_triggerrate", defaultValue = "false")
	public static boolean SKILL_CHAIN_DISABLE_TRIGGERRATE;

	/**
	 * Base Fly Time
	 */
	@Property(key = "gameserver.base.flytime", defaultValue = "60")
	public static int BASE_FLYTIME;

	@Property(key = "gameserver.friendlist.gm_restrict", defaultValue = "false")
	public static boolean FRIENDLIST_GM_RESTRICT;

	/**
	 * Friendlist size
	 */
	@Property(key = "gameserver.friendlist.size", defaultValue = "90")
	public static int FRIENDLIST_SIZE;

	/**
	 * Basic Quest limit size
	 */
	@Property(key = "gameserver.basic.questsize.limit", defaultValue = "40")
	public static int BASIC_QUEST_SIZE_LIMIT;

	/**
	 * Total number of allowed cube expansions
	 */
	@Property(key = "gameserver.cube.expansion_limit", defaultValue = "11")
	public static int CUBE_EXPANSION_LIMIT;

	/**
	 * Npc Cube Expands limit size
	 */
	@Property(key = "gameserver.npcexpands.limit", defaultValue = "5")
	public static int NPC_CUBE_EXPANDS_SIZE_LIMIT;

	/**
	 * Enable Kinah cap
	 */
	@Property(key = "gameserver.enable.kinah.cap", defaultValue = "false")
	public static boolean ENABLE_KINAH_CAP;

	/**
	 * Kinah cap value
	 */
	@Property(key = "gameserver.kinah.cap.value", defaultValue = "999999999")
	public static long KINAH_CAP_VALUE;

	/**
	 * Enable AP cap
	 */
	@Property(key = "gameserver.enable.ap.cap", defaultValue = "false")
	public static boolean ENABLE_AP_CAP;

	/**
	 * AP cap value
	 */
	@Property(key = "gameserver.ap.cap.value", defaultValue = "1000000")
	public static long AP_CAP_VALUE;

	/**
	 * Enable no AP in mentored group.
	 */
	@Property(key = "gameserver.noap.mentor.group", defaultValue = "false")
	public static boolean MENTOR_GROUP_AP;

	/**
	 * .faction cfg
	 */
	@Property(key = "gameserver.faction.price", defaultValue = "10000")
	public static int FACTION_USE_PRICE;

	@Property(key = "gameserver.faction.cmdchannel", defaultValue = "true")
	public static boolean FACTION_CMD_CHANNEL;

	@Property(key = "gameserver.faction.chatchannels", defaultValue = "false")
	public static boolean FACTION_CHAT_CHANNEL;

	/**
	 * Time in milliseconds in which players are limited for killing one player
	 */
	@Property(key = "gameserver.pvp.dayduration", defaultValue = "86400000")
	public static long PVP_DAY_DURATION;

	/**
	 * Allowed Kills in configuered time for full AP. Move to separate config when more pvp options.
	 */
	@Property(key = "gameserver.pvp.maxkills", defaultValue = "5")
	public static int MAX_DAILY_PVP_KILLS;

	/**
	 * Add a reward to player for pvp kills
	 */
	@Property(key = "gameserver.kill.reward.enable", defaultValue = "false")
	public static boolean ENABLE_KILL_REWARD;

	/**
	 * Enable one kisk restriction
	 */
	@Property(key = "gameserver.kisk.restriction.enable", defaultValue = "true")
	public static boolean ENABLE_KISK_RESTRICTION;

	@Property(key = "gameserver.rift.enable", defaultValue = "true")
	public static boolean RIFT_ENABLED;
	@Property(key = "gameserver.rift.duration", defaultValue = "1")
	public static int RIFT_DURATION;

	@Property(key = "gameserver.vortex.enable", defaultValue = "true")
	public static boolean VORTEX_ENABLED;
	@Property(key = "gameserver.vortex.brusthonin.schedule", defaultValue = "0 0 16 ? * SAT")
	public static CronExpression VORTEX_BRUSTHONIN_SCHEDULE;
	@Property(key = "gameserver.vortex.theobomos.schedule", defaultValue = "0 0 16 ? * SUN")
	public static CronExpression VORTEX_THEOBOMOS_SCHEDULE;
	@Property(key = "gameserver.vortex.duration", defaultValue = "1")
	public static int VORTEX_DURATION;

	@Property(key = "gameserver.cp.enable", defaultValue = "true")
	public static boolean CONQUEROR_AND_PROTECTOR_SYSTEM_ENABLED;
	@Property(key = "gameserver.cp.worlds", defaultValue = "210020000,210040000,210050000,210070000,220020000,220040000,220070000,220080000")
	public static Set<Integer> CONQUEROR_AND_PROTECTOR_WORLDS;
	@Property(key = "gameserver.cp.level.diff", defaultValue = "5")
	public static int CONQUEROR_AND_PROTECTOR_LEVEL_DIFF;
	@Property(key = "gameserver.cp.kills.decrease_interval_minutes", defaultValue = "10")
	public static int CONQUEROR_AND_PROTECTOR_KILLS_DECREASE_INTERVAL;
	@Property(key = "gameserver.cp.kills.decrease_count", defaultValue = "1")
	public static int CONQUEROR_AND_PROTECTOR_KILLS_DECREASE_COUNT;
	@Property(key = "gameserver.cp.kills.rank1", defaultValue = "1")
	public static int CONQUEROR_AND_PROTECTOR_KILLS_RANK1;
	@Property(key = "gameserver.cp.kills.rank2", defaultValue = "10")
	public static int CONQUEROR_AND_PROTECTOR_KILLS_RANK2;
	@Property(key = "gameserver.cp.kills.rank3", defaultValue = "20")
	public static int CONQUEROR_AND_PROTECTOR_KILLS_RANK3;

	/**
	 * Limits Config
	 */
	@Property(key = "gameserver.limits.enable", defaultValue = "true")
	public static boolean LIMITS_ENABLED;

	@Property(key = "gameserver.limits.enable_dynamic_cap", defaultValue = "false")
	public static boolean LIMITS_ENABLE_DYNAMIC_CAP;

	@Property(key = "gameserver.limits.update", defaultValue = "0 0 0 ? * *")
	public static CronExpression LIMITS_UPDATE;

	@Property(key = "gameserver.abyssxform.afterlogout", defaultValue = "false")
	public static boolean ABYSSXFORM_LOGOUT;

	@Property(key = "gameserver.ride.restriction.enable", defaultValue = "true")
	public static boolean ENABLE_RIDE_RESTRICTION;

	/**
	 * Enables sell apitems
	 */
	@Property(key = "gameserver.selling.apitems.enabled", defaultValue = "true")
	public static boolean SELLING_APITEMS_ENABLED;

	@Property(key = "character.deletion.time.minutes", defaultValue = "5")
	public static int CHARACTER_DELETION_TIME_MINUTES;

	/**
	 * Custom Reward Packages
	 */
	@Property(key = "gameserver.custom.starter_kit.enable", defaultValue = "false")
	public static boolean ENABLE_STARTER_KIT;

	@Property(key = "gameserver.pvpmap.enable", defaultValue = "false")
	public static boolean PVP_MAP_ENABLED;

	@Property(key = "gameserver.pvpmap.apmultiplier", defaultValue = "2")
	public static float PVP_MAP_AP_MULTIPLIER;

	@Property(key = "gameserver.pvpmap.pve.apmultiplier", defaultValue = "1")
	public static float PVP_MAP_PVE_AP_MULTIPLIER;

	@Property(key = "gameserver.pvpmap.random_boss.rate", defaultValue = "40")
	public static int PVP_MAP_RANDOM_BOSS_BASE_RATE;

	@Property(key = "gameserver.pvpmap.random_boss.time", defaultValue = "0 30 14,18,21 ? * *")
	public static CronExpression PVP_MAP_RANDOM_BOSS_SCHEDULE;

	@Property(key = "gameserver.rates.godstone.activation.rate", defaultValue = "1.0")
	public static float GODSTONE_ACTIVATION_RATE;

	@Property(key = "gameserver.rates.godstone.evaluation.cooldown_millis", defaultValue = "500")
	public static int GODSTONE_EVALUATION_COOLDOWN_MILLIS;
}
