package com.aionemu.gameserver.model.stats.calc;

import java.util.EnumMap;

import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.stats.container.StatEnum;

/**
 * @author ATracer, Neon
 */
public class StatCapUtil {

	private static final EnumMap<StatEnum, StatLimits> limits = new EnumMap<>(StatEnum.class);

	static {
		for (StatEnum stat : StatEnum.values()) {
			limits.put(stat, new StatLimits(stat));
		}
	}

	public static void calculateBaseValue(Stat2 stat, Creature creature) {
		int lowerCap = getLowerCap(stat.getStat());
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

	public static int getLowerCap(StatEnum stat) {
		return limits.get(stat).lowerCap;
	}

	public static int getUpperCap(StatEnum stat, Creature creature) {
		boolean isSpeedUnrestricted = !(creature instanceof Player) || ((Player) creature).isStaff();
		if ((stat == StatEnum.SPEED || stat == StatEnum.FLY_SPEED) && isSpeedUnrestricted)
			return Integer.MAX_VALUE;
		return limits.get(stat).upperCap;
	}

	public static int getDifferenceLimit(StatEnum stat) {
		return limits.get(stat).diffLimit;
	}

	private static void calculate(Stat2 stat2, int lowerCap, int upperCap) {
		if (stat2.getCurrent() > upperCap) {
			stat2.setBonus(upperCap - stat2.getBase());
		} else if (stat2.getCurrent() < lowerCap) {
			stat2.setBonus(lowerCap - stat2.getBase());
		}
	}

	private static class StatLimits {

		private final int lowerCap;
		private final int upperCap;
		private final int diffLimit;

		private StatLimits(StatEnum stat) {
			this.lowerCap = lowerCapFor(stat);
			this.upperCap = upperCapFor(stat);
			this.diffLimit = differenceLimitFor(stat);
		}

		private static int lowerCapFor(StatEnum stat) {
			int value = Integer.MIN_VALUE;
			switch (stat) {
				case MAIN_HAND_POWER:
				case MAIN_HAND_ACCURACY:
				case MAIN_HAND_CRITICAL:
				case OFF_HAND_POWER:
				case OFF_HAND_ACCURACY:
				case OFF_HAND_CRITICAL:
				case MAGICAL_RESIST:
				case PHYSICAL_CRITICAL_RESIST:
				case EVASION:
				case PHYSICAL_DEFENSE:
				case PHYSICAL_ACCURACY:
				case MAGICAL_ACCURACY:
				case SPEED:
				case FLY_SPEED:
				case MAXHP:
				case MAXMP:
					value = 0;
					break;
				case WATER_RESISTANCE:
				case FIRE_RESISTANCE:
				case EARTH_RESISTANCE:
				case WIND_RESISTANCE:
				case DARK_RESISTANCE:
				case LIGHT_RESISTANCE:
					value = -1150;
					break;
			}
			return value;
		}

		private static int upperCapFor(StatEnum stat) {
			int value = Integer.MAX_VALUE;
			switch (stat) {
				case SPEED:
					value = 12000;
					break;
				case FLY_SPEED:
					value = 16000;
					break;
				case PVP_DEFEND_RATIO:
					value = 900;
					break;
				case HEAL_BOOST:
					value = 1000;
					break;
				case WATER_RESISTANCE:
				case FIRE_RESISTANCE:
				case EARTH_RESISTANCE:
				case WIND_RESISTANCE:
				case DARK_RESISTANCE:
				case LIGHT_RESISTANCE:
					value = 1150;
					break;
			}
			return value;
		}

		private static int differenceLimitFor(StatEnum stat) {
			switch (stat) {
				case BLOCK:
				case PHYSICAL_CRITICAL:
				case MAGICAL_CRITICAL:
					return 500;
				case MAGICAL_RESIST:
					return 900; // in PvP: 500 (see StatFunctions#calculateMagicalResistRate)
				case EVASION:
					return 300;
				case PARRY:
					return 400;
				case BOOST_MAGICAL_SKILL:
					return 2900;
			}
			return Integer.MAX_VALUE;
		}
	}
}
