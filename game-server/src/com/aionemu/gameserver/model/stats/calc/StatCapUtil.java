package com.aionemu.gameserver.model.stats.calc;

import java.util.EnumMap;
import java.util.EnumSet;

import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.stats.container.CombatMode;
import com.aionemu.gameserver.model.stats.container.RatioType;
import com.aionemu.gameserver.model.stats.container.StatEnum;

/**
 * @author ATracer, Neon
 */
public class StatCapUtil {

	private static final EnumMap<StatEnum, StatCapRule> limits = new EnumMap<>(StatEnum.class);

	static {
		registerDefaults();
	}

	private static void registerDefaults() {
		registerLower(0, StatEnum.MAIN_HAND_POWER, StatEnum.MAIN_HAND_ACCURACY, StatEnum.MAIN_HAND_CRITICAL, StatEnum.OFF_HAND_POWER,
			StatEnum.OFF_HAND_ACCURACY, StatEnum.OFF_HAND_CRITICAL, StatEnum.MAGICAL_RESIST, StatEnum.PHYSICAL_CRITICAL_RESIST,
			StatEnum.MAGICAL_CRITICAL_RESIST, StatEnum.PHYSICAL_CRITICAL_DAMAGE_REDUCE, StatEnum.MAGICAL_CRITICAL_DAMAGE_REDUCE, StatEnum.EVASION,
			StatEnum.PHYSICAL_DEFENSE, StatEnum.PHYSICAL_ACCURACY, StatEnum.MAGICAL_ACCURACY);

		register(StatEnum.SPEED, 0, 12000);
		register(StatEnum.FLY_SPEED, 0, 16000);
		register(StatEnum.HEAL_BOOST, -1000, 1000);
		register(StatEnum.MAXHP, creature -> creature instanceof Player ? 100 : 1, CapFunction.UNLIMITED_UPPER);
		register(StatEnum.MAXMP, creature -> creature instanceof Player ? 1 : 0, CapFunction.UNLIMITED_UPPER);

		for (StatEnum statEnum : EnumSet.of(StatEnum.POWER, StatEnum.AGILITY, StatEnum.ACCURACY, StatEnum.HEALTH, StatEnum.KNOWLEDGE, StatEnum.WILL)) {
			register(statEnum, 80, 999);
		}

		registerUpper(700, StatEnum.PHYSICAL_CRITICAL_RESIST, StatEnum.MAGICAL_CRITICAL_RESIST, StatEnum.PHYSICAL_CRITICAL_DAMAGE_REDUCE,
			StatEnum.MAGICAL_CRITICAL_DAMAGE_REDUCE);

		registerDifferenceLimit(500, StatEnum.BLOCK, StatEnum.PHYSICAL_CRITICAL, StatEnum.MAGICAL_CRITICAL);
		registerDifferenceLimit(900, StatEnum.MAGICAL_RESIST); // in PvP: 500 (see StatFunctions#calculateMagicalResistRate)
		registerDifferenceLimit(300, StatEnum.EVASION);
		registerDifferenceLimit(400, StatEnum.PARRY);
		registerDifferenceLimit(2900, StatEnum.BOOST_MAGICAL_SKILL);

		registerElementalDefense(StatEnum.WATER_RESISTANCE, StatEnum.FIRE_RESISTANCE, StatEnum.EARTH_RESISTANCE, StatEnum.WIND_RESISTANCE,
			StatEnum.DARK_RESISTANCE, StatEnum.LIGHT_RESISTANCE);
	}

	public static int getElementalDefenseBaseValue() {
		return 1300;
	}

	public static void calculateBaseValue(Stat2 stat, Creature creature) {
		int lowerCap = getLowerCap(stat.getStat(), creature);
		int upperCap = getUpperCap(stat.getStat(), creature);

		if (stat.getStat() == StatEnum.ATTACK_SPEED) {
			int base = stat.getBase() / 2;
			if (stat.getBonus() > 0 && base < stat.getBonus())
				stat.setBonus(base);
			else if (stat.getBonus() < 0 && base < -stat.getBonus())
				stat.setBonus(-base);
		}

		calculate(stat, lowerCap, upperCap);
	}

	public static int getLowerCap(StatEnum stat, Creature creature) {
		return getRule(stat).lowerCap().apply(creature);
	}

	public static int getUpperCap(StatEnum stat, Creature creature) {
		boolean isSpeedUnrestricted = !(creature instanceof Player player) || player.isStaff();
		if ((stat == StatEnum.SPEED || stat == StatEnum.FLY_SPEED) && isSpeedUnrestricted)
			return Integer.MAX_VALUE;
		return getRule(stat).upperCap().apply(creature);
	}

	public static int getElementalDefenseCapForCreature(Creature creature) {
		if (creature instanceof Player) {
			return 1000 + Math.max(0, creature.getLevel() - 50) * 10;
		}
		return getElementalDefenseBaseValue();
	}

	public static int getDifferenceLimit(StatEnum stat) {
		return getRule(stat).diffLimit();
	}

	public static int clampStatValue(StatEnum stat, Creature creature, int value) {
		int lower = getLowerCap(stat, creature);
		int upper = getUpperCap(stat, creature);
		return Math.clamp(value, lower, upper);
	}

	public static int limitValueForPvpOrPveStat(CombatMode mode, RatioType type, int value) {
		// Note: PvP/PvE ratio caps are symmetric:
		// - attack min is fixed, defense max is fixed
		// - upper/lower bounds depend on combat mode
		Cap cap = switch (mode) {
			case PVP -> switch (type) {
				case ATTACK -> new Cap(-900, 1000);
				case DEFENSE -> new Cap(-1000, 900);
			};
			case PVE -> switch (type) {
				case ATTACK -> new Cap(-900, 5000);
				case DEFENSE -> new Cap(-5000, 900);
			};
		};

		return Math.clamp(value, cap.min(), cap.max());
	}

	private static void calculate(Stat2 stat2, int lowerCap, int upperCap) {
		if (stat2.getCurrent() > upperCap) {
			stat2.setBonus(upperCap - stat2.getBase());
		} else if (stat2.getCurrent() < lowerCap) {
			stat2.setBonus(lowerCap - stat2.getBase());
		}
	}

	private static void register(StatEnum stat, int lowerCap, int upperCap) {
		register(stat, _ -> lowerCap, _ -> upperCap, Integer.MAX_VALUE);
	}

	private static void register(StatEnum stat, CapFunction lowerCap, CapFunction upperCap) {
		register(stat, lowerCap, upperCap, Integer.MAX_VALUE);
	}

	private static void register(StatEnum stat, CapFunction lowerCap, CapFunction upperCap, int diffLimit) {
		limits.put(stat, new StatCapRule(lowerCap, upperCap, diffLimit));
	}

	private static void registerLower(int lowerCap, StatEnum... stats) {
		registerLower(_ -> lowerCap, stats);
	}

	private static void registerLower(CapFunction lowerCap, StatEnum... stats) {
		for (StatEnum stat : stats) {
			limits.compute(stat, (_, rule) -> rule == null ? new StatCapRule(lowerCap, CapFunction.UNLIMITED_UPPER, Integer.MAX_VALUE)
				: new StatCapRule(lowerCap, rule.upperCap(), rule.diffLimit()));
		}
	}

	private static void registerUpper(int upperCap, StatEnum... stats) {
		registerUpper(_ -> upperCap, stats);
	}

	private static void registerUpper(CapFunction upperCap, StatEnum... stats) {
		for (StatEnum stat : stats) {
			limits.compute(stat, (_, rule) -> rule == null ? new StatCapRule(CapFunction.UNLIMITED_LOWER, upperCap, Integer.MAX_VALUE)
				: new StatCapRule(rule.lowerCap(), upperCap, rule.diffLimit()));
		}
	}

	private static void registerDifferenceLimit(int diffLimit, StatEnum... stats) {
		for (StatEnum stat : stats) {
			limits.compute(stat, (_, rule) -> rule == null ? new StatCapRule(CapFunction.UNLIMITED_LOWER, CapFunction.UNLIMITED_UPPER, diffLimit)
				: new StatCapRule(rule.lowerCap(), rule.upperCap(), diffLimit));
		}
	}

	private static void registerElementalDefense(StatEnum... stats) {
		for (StatEnum stat : stats) {
			register(stat, creature -> -getElementalDefenseCapForCreature(creature), StatCapUtil::getElementalDefenseCapForCreature);
		}
	}

	private static StatCapRule getRule(StatEnum stat) {
		return limits.getOrDefault(stat, StatCapRule.UNLIMITED);
	}

	private record Cap(int min, int max) {}

	@FunctionalInterface
	private interface CapFunction {

		CapFunction UNLIMITED_LOWER = _ -> Integer.MIN_VALUE;
		CapFunction UNLIMITED_UPPER = _ -> Integer.MAX_VALUE;

		int apply(Creature creature);
	}

	private record StatCapRule(CapFunction lowerCap, CapFunction upperCap, int diffLimit) {

		private static final StatCapRule UNLIMITED = new StatCapRule(CapFunction.UNLIMITED_LOWER, CapFunction.UNLIMITED_UPPER, Integer.MAX_VALUE);
	}
}
