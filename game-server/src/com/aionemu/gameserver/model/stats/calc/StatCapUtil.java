package com.aionemu.gameserver.model.stats.calc;

import static com.aionemu.gameserver.model.stats.container.StatEnum.*;

import java.util.EnumMap;
import java.util.List;

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
		register(MAXHP, creature -> creature instanceof Player ? 100 : 1, CapFunction.UNLIMITED_UPPER);
		register(MAXMP, creature -> creature instanceof Player ? 1 : 0, CapFunction.UNLIMITED_UPPER);
		register(SPEED, 0, creature -> creature instanceof Player p && !p.isStaff() ? 12000 : Integer.MAX_VALUE);
		register(FLY_SPEED, 0, creature -> creature instanceof Player p && !p.isStaff() ? 16000 : Integer.MAX_VALUE);
		register(HEAL_BOOST, -1000, 1000);
		register(EVASION, 0, CapFunction.UNLIMITED_UPPER, 300);
		register(PARRY, 0, CapFunction.UNLIMITED_UPPER, 400);
		register(BLOCK, 0, CapFunction.UNLIMITED_UPPER, 500);
		register(PHYSICAL_CRITICAL, 0, CapFunction.UNLIMITED_UPPER, 500);
		register(MAGICAL_CRITICAL, 0, CapFunction.UNLIMITED_UPPER, 500);
		register(MAGICAL_RESIST, 0, CapFunction.UNLIMITED_UPPER, 900); // diffLimit in PvP: 500 (see StatFunctions#calculateMagicalResistRate)
		register(BOOST_MAGICAL_SKILL, 0, CapFunction.UNLIMITED_UPPER, 2900);
		for (StatEnum stat : List.of(PHYSICAL_CRITICAL_RESIST, MAGICAL_CRITICAL_RESIST, PHYSICAL_CRITICAL_DAMAGE_REDUCE, MAGICAL_CRITICAL_DAMAGE_REDUCE))
			register(stat, 0, 700);
		for (StatEnum stat : List.of(POWER, AGILITY, ACCURACY, HEALTH, KNOWLEDGE, WILL))
			register(stat, 80, 999);
		for (StatEnum stat : List.of(MAIN_HAND_POWER, MAIN_HAND_ACCURACY, MAIN_HAND_CRITICAL, OFF_HAND_POWER, OFF_HAND_ACCURACY, OFF_HAND_CRITICAL,
			PHYSICAL_DEFENSE, PHYSICAL_ACCURACY, MAGICAL_ACCURACY))
			register(stat, 0, CapFunction.UNLIMITED_UPPER);
		for (StatEnum stat : List.of(WATER_RESISTANCE, FIRE_RESISTANCE, EARTH_RESISTANCE, WIND_RESISTANCE, DARK_RESISTANCE, LIGHT_RESISTANCE))
			register(stat, creature -> -getElementalDefenseCapForCreature(creature), StatCapUtil::getElementalDefenseCapForCreature);
	}

	public static int getElementalDefenseBaseValue() {
		return 1300;
	}

	public static void calculateBaseValue(Stat2 stat, Creature creature) {
		int lowerCap = getLowerCap(stat.getStat(), creature);
		int upperCap = getUpperCap(stat.getStat(), creature);

		if (stat.getStat() == ATTACK_SPEED) {
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

	private static void register(StatEnum stat, int lowerCap, CapFunction upperCap) {
		register(stat, _ -> lowerCap, upperCap, Integer.MAX_VALUE);
	}

	private static void register(StatEnum stat, int lowerCap, CapFunction upperCap, int diffLimit) {
		register(stat, _ -> lowerCap, upperCap, diffLimit);
	}

	private static void register(StatEnum stat, CapFunction lowerCap, CapFunction upperCap, int diffLimit) {
		if (limits.putIfAbsent(stat, new StatCapRule(lowerCap, upperCap, diffLimit)) != null)
			throw new IllegalArgumentException("A limit for " + stat + " is already registered");
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
