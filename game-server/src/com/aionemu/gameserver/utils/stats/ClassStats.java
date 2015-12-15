package com.aionemu.gameserver.utils.stats;

import com.aionemu.gameserver.model.PlayerClass;
import com.aionemu.gameserver.utils.stats.enums.ACCURACY;
import com.aionemu.gameserver.utils.stats.enums.AGILITY;
import com.aionemu.gameserver.utils.stats.enums.ATTACK_RANGE;
import com.aionemu.gameserver.utils.stats.enums.ATTACK_SPEED;
import com.aionemu.gameserver.utils.stats.enums.BLOCK;
import com.aionemu.gameserver.utils.stats.enums.EARTH_RESIST;
import com.aionemu.gameserver.utils.stats.enums.EVASION;
import com.aionemu.gameserver.utils.stats.enums.FIRE_RESIST;
import com.aionemu.gameserver.utils.stats.enums.FLY_SPEED;
import com.aionemu.gameserver.utils.stats.enums.HEALTH;
import com.aionemu.gameserver.utils.stats.enums.KNOWLEDGE;
import com.aionemu.gameserver.utils.stats.enums.MAGIC_ACCURACY;
import com.aionemu.gameserver.utils.stats.enums.MAIN_HAND_ACCURACY;
import com.aionemu.gameserver.utils.stats.enums.MAIN_HAND_ATTACK;
import com.aionemu.gameserver.utils.stats.enums.MAIN_HAND_CRITRATE;
import com.aionemu.gameserver.utils.stats.enums.MAXHP;
import com.aionemu.gameserver.utils.stats.enums.PARRY;
import com.aionemu.gameserver.utils.stats.enums.POWER;
import com.aionemu.gameserver.utils.stats.enums.SPEED;
import com.aionemu.gameserver.utils.stats.enums.WATER_RESIST;
import com.aionemu.gameserver.utils.stats.enums.WILL;
import com.aionemu.gameserver.utils.stats.enums.WIND_RESIST;

/**
 * @author ATracer
 */
public class ClassStats {

	/**
	 * @param playerClass
	 * @param level
	 * @return maximum HP stat for player class and level
	 */
	public static int getMaxHpFor(PlayerClass playerClass, int level) {
		return MAXHP.valueOf(playerClass.toString()).getMaxHpFor(level);
	}

	/**
	 * @param playerClass
	 * @return power stat for player class and level
	 */
	public static int getPowerFor(PlayerClass playerClass) {
		return POWER.valueOf(playerClass.toString()).getValue();
	}

	/**
	 * @param playerClass
	 * @return int
	 */
	public static int getHealthFor(PlayerClass playerClass) {
		return HEALTH.valueOf(playerClass.toString()).getValue();
	}

	/**
	 * @param playerClass
	 * @return int
	 */
	public static int getAgilityFor(PlayerClass playerClass) {
		return AGILITY.valueOf(playerClass.toString()).getValue();
	}

	/**
	 * @param playerClass
	 * @return int
	 */
	public static int getBaseAccuracyFor(PlayerClass playerClass) {
		return ACCURACY.valueOf(playerClass.toString()).getValue();
	}

	/**
	 * @param playerClass
	 * @return int
	 */
	public static int getKnowledgeFor(PlayerClass playerClass) {
		return KNOWLEDGE.valueOf(playerClass.toString()).getValue();
	}

	/**
	 * @param playerClass
	 * @return int
	 */
	public static int getWillFor(PlayerClass playerClass) {
		return WILL.valueOf(playerClass.toString()).getValue();
	}

	/**
	 * @param playerClass
	 * @return int
	 */
	public static int getAttackFor(PlayerClass playerClass) {
		return MAIN_HAND_ATTACK.valueOf(playerClass.toString()).getValue();
	}

	/**
	 * @param playerClass
	 * @return int
	 */
	public static int getCritFor(PlayerClass playerClass) {
		return MAIN_HAND_CRITRATE.valueOf(playerClass.toString()).getValue();
	}

	/**
	 * @param playerClass
	 * @return int
	 */
	public static int getAccuracyFor(PlayerClass playerClass) {
		return MAIN_HAND_ACCURACY.valueOf(playerClass.toString()).getValue();
	}

	/**
	 * @param playerClass
	 * @return int
	 */
	public static int getWaterResistFor(PlayerClass playerClass) {
		return WATER_RESIST.valueOf(playerClass.toString()).getValue();
	}

	/**
	 * @param playerClass
	 * @return int
	 */
	public static int getWindResistFor(PlayerClass playerClass) {
		return WIND_RESIST.valueOf(playerClass.toString()).getValue();
	}

	/**
	 * @param playerClass
	 * @return int
	 */
	public static int getEarthResistFor(PlayerClass playerClass) {
		return EARTH_RESIST.valueOf(playerClass.toString()).getValue();
	}

	/**
	 * @param playerClass
	 * @return int
	 */
	public static int getFireResistFor(PlayerClass playerClass) {
		return FIRE_RESIST.valueOf(playerClass.toString()).getValue();
	}

	/**
	 * @param playerClass
	 * @return int
	 */
	public static int getMagicAccuracyFor(PlayerClass playerClass) {
		return MAGIC_ACCURACY.valueOf(playerClass.toString()).getValue();
	}

	/**
	 * @param playerClass
	 * @return int
	 */
	public static int getEvasionFor(PlayerClass playerClass) {
		return EVASION.valueOf(playerClass.toString()).getValue();
	}

	/**
	 * @param playerClass
	 * @return int
	 */
	public static int getBlockFor(PlayerClass playerClass) {
		return BLOCK.valueOf(playerClass.toString()).getValue();
	}

	/**
	 * @param playerClass
	 * @return int
	 */
	public static int getParryFor(PlayerClass playerClass) {
		return PARRY.valueOf(playerClass.toString()).getValue();
	}

	/**
	 * @param playerClass
	 * @return int
	 */
	public static int getAttackRangeFor(PlayerClass playerClass) {
		return ATTACK_RANGE.valueOf(playerClass.toString()).getValue();
	}

	/**
	 * @param playerClass
	 * @return int
	 */
	public static int getAttackSpeedFor(PlayerClass playerClass) {
		return ATTACK_SPEED.valueOf(playerClass.toString()).getValue();
	}

	/**
	 * @param playerClass
	 * @return int
	 */
	public static int getFlySpeedFor(PlayerClass playerClass) {
		return FLY_SPEED.valueOf(playerClass.toString()).getValue();
	}

	/**
	 * @param playerClass
	 * @return int
	 */
	public static int getSpeedFor(PlayerClass playerClass) {
		return SPEED.valueOf(playerClass.toString()).getValue();
	}
}
