package com.aionemu.gameserver.model.stats.calc;

import com.aionemu.gameserver.model.stats.container.StatEnum;
import com.aionemu.gameserver.model.templates.npc.NpcRank;
import com.aionemu.gameserver.model.templates.npc.NpcRating;

/**
 * @author Estrayl, Neon
 */
public class NpcStatCalculation {

	public static int calculateStat(StatEnum stat, NpcRating rating, NpcRank rank, byte level) {
		float baseValue = getBaseValue(stat, level);
		float ratingMod = getRatingModifier(stat, rating);
		float rankMod = getRankModifier(stat, rank);
		return Math.round(baseValue * ratingMod * rankMod);
	}

	private static float getBaseValue(StatEnum stat, byte level) {
		return switch (stat) {
			// https://www.wolframalpha.com/input/?i=-0.0007x%5E3+%2B+0.1x%5E2+%2B+5.3x
			case PHYSICAL_ATTACK -> -0.0007f * (float) Math.pow(level, 3) + 0.1f * (float) Math.pow(level, 2) + 5.3f * level;
			case MAGICAL_DEFEND -> level * 5f;
			case MAGICAL_ATTACK -> level * 20f;
			case PHYSICAL_DEFENSE -> level * 17f;
			case MAGICAL_ACCURACY -> level * 25f;
			// formula with help of https://www.wolframalpha.com/input/?i=fit+(1,20),(15,270),(30,585),(50,1075),(60,1350),(65,1495)
			case MAGICAL_RESIST -> 0.1f * (float) Math.pow(level, 2) + 16.5f * level;
			case PHYSICAL_ACCURACY -> level * 37f;
			case PARRY -> level * 40f;
			case PHYSICAL_CRITICAL_RESIST -> (level - 50) * 2.5f;
			case MAGICAL_CRITICAL_RESIST -> (level - 50) * 1.1f;
			case ABNORMAL_RESISTANCE_ALL -> 100f;
			default -> throw new IllegalArgumentException("Stat calculation for " + stat + " is not implemented");
		};
	}

	private static float getRatingModifier(StatEnum stat, NpcRating rating) {
		return switch (rating) {
			case JUNK, NORMAL -> switch (stat) {
					case MAGICAL_ATTACK -> 0.4f;
					case ABNORMAL_RESISTANCE_ALL -> 0f;
					default -> 1f;
				};
			case ELITE -> switch (stat) {
					case PHYSICAL_ATTACK -> 1.7f;
					case MAGICAL_ATTACK -> 0.5f;
					case MAGICAL_DEFEND, PHYSICAL_DEFENSE -> 1.25f;
					case MAGICAL_RESIST -> 1.05f;
					case PHYSICAL_ACCURACY, MAGICAL_ACCURACY -> 1.03f;
					case PARRY -> 1.025f;
					case PHYSICAL_CRITICAL_RESIST -> 9f;
					case MAGICAL_CRITICAL_RESIST -> 8.5f;
					case ABNORMAL_RESISTANCE_ALL -> 5f;
					default -> 1f;
				};
			case HERO -> switch (stat) {
					case PHYSICAL_ATTACK -> 2.4f;
					case MAGICAL_ATTACK -> 0.6f;
					case PHYSICAL_ACCURACY, MAGICAL_ACCURACY -> 1.075f;
					case MAGICAL_RESIST -> 1.2f;
					case MAGICAL_DEFEND, PHYSICAL_DEFENSE -> 1.5f;
					case PARRY -> 1.07f;
					case PHYSICAL_CRITICAL_RESIST, MAGICAL_CRITICAL_RESIST -> 13.5f;
					case ABNORMAL_RESISTANCE_ALL -> 20f;
					default -> 1f;
				};
			case LEGENDARY -> switch (stat) {
					case PHYSICAL_ATTACK -> 2.6f;
					case PHYSICAL_DEFENSE, MAGICAL_DEFEND -> 1.75f;
					case MAGICAL_RESIST -> 1.35f;
					case MAGICAL_ACCURACY -> 1.47f;
					case MAGICAL_ATTACK, PARRY, PHYSICAL_ACCURACY -> 1.1f;
					case PHYSICAL_CRITICAL_RESIST, MAGICAL_CRITICAL_RESIST -> 13.5f;
					case ABNORMAL_RESISTANCE_ALL -> 20f;
					default -> 1f;
				};
		};
	}

	private static float getRankModifier(StatEnum stat, NpcRank rank) {
		return switch (rank) {
			case NOVICE -> switch (stat) {
					case ABNORMAL_RESISTANCE_ALL -> 0.2f;
					default -> 1f;
				};
			case DISCIPLINED -> switch (stat) {
					case PHYSICAL_ATTACK, PHYSICAL_CRITICAL_RESIST -> 1.2f;
					case MAGICAL_RESIST -> 1.02f;
					case MAGICAL_DEFEND, PHYSICAL_DEFENSE -> 1.1f;
					case MAGICAL_ATTACK -> 1.45f;
					case PARRY -> 1.05f;
					case ABNORMAL_RESISTANCE_ALL -> 0.4f;
					default -> 1f;
				};
			case SEASONED -> switch (stat) {
					case PHYSICAL_ATTACK -> 1.6f;
					case MAGICAL_DEFEND, PHYSICAL_DEFENSE -> 1.2f;
					case MAGICAL_RESIST -> 1.03f;
					case MAGICAL_ATTACK -> 1.45f;
					case PARRY -> 1.1f;
					case PHYSICAL_ACCURACY, MAGICAL_ACCURACY -> 1.01f;
					case PHYSICAL_CRITICAL_RESIST -> 1.4f;
					case ABNORMAL_RESISTANCE_ALL -> 0.6f;
					default -> 1f;
				};
			case EXPERT -> switch (stat) {
					case PHYSICAL_ATTACK -> 1.65f;
					case MAGICAL_RESIST -> 1.04f;
					case MAGICAL_DEFEND, PHYSICAL_DEFENSE -> 1.3f;
					case MAGICAL_ATTACK -> 1.7f;
					case PARRY -> 1.1f;
					case PHYSICAL_ACCURACY, MAGICAL_ACCURACY -> 1.02f;
					case PHYSICAL_CRITICAL_RESIST -> 1.6f;
					case MAGICAL_CRITICAL_RESIST -> 1.2f;
					default -> 1f;
				};
			case VETERAN -> switch (stat) {
					case PHYSICAL_ATTACK, MAGICAL_ATTACK -> 1.7f;
					case MAGICAL_DEFEND, PHYSICAL_DEFENSE, ABNORMAL_RESISTANCE_ALL -> 1.4f;
					case MAGICAL_RESIST -> 1.05f;
					case PARRY -> 1.12f;
					case PHYSICAL_ACCURACY, MAGICAL_ACCURACY -> 1.03f;
					case PHYSICAL_CRITICAL_RESIST -> 1.8f;
					case MAGICAL_CRITICAL_RESIST -> 1.25f;
					default -> 1f;
				};
			case MASTER -> switch (stat) {
					case PHYSICAL_ATTACK -> 1.85f;
					case MAGICAL_DEFEND, PHYSICAL_DEFENSE -> 1.5f;
					case MAGICAL_RESIST -> 1.06f;
					case MAGICAL_ATTACK, ABNORMAL_RESISTANCE_ALL -> 1.7f;
					case PARRY -> 1.12f;
					case PHYSICAL_ACCURACY, MAGICAL_ACCURACY -> 1.04f;
					case PHYSICAL_CRITICAL_RESIST -> 1.8f;
					case MAGICAL_CRITICAL_RESIST -> 1.25f;
					default -> 1f;
				};
		};
	}
}
