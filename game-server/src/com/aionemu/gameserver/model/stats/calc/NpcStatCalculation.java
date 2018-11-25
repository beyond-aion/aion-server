package com.aionemu.gameserver.model.stats.calc;

import com.aionemu.gameserver.model.stats.container.StatEnum;
import com.aionemu.gameserver.model.templates.npc.NpcRank;
import com.aionemu.gameserver.model.templates.npc.NpcRating;

/**
 * @author Estrayl
 * @modified Neon
 */
public class NpcStatCalculation {

	public static int calculateStat(StatEnum stat, NpcRating rating, NpcRank rank, byte level) {
		float baseValue = getBaseValue(stat, level);
		float ratingMod = getRatingModifier(stat, rating);
		float rankMod = getRankModifier(stat, rank);
		return Math.round(baseValue * ratingMod * rankMod);
	}

	private static float getBaseValue(StatEnum stat, byte level) {
		switch (stat) {
			case PHYSICAL_ATTACK: // https://www.wolframalpha.com/input/?i=-0.0007x%5E3+%2B+0.1x%5E2+%2B+5.3x
				return -0.0007f * (float) Math.pow(level, 3) + 0.1f * (float) Math.pow(level, 2) + 5.3f * level;
			case MAGICAL_DEFEND:
				return level * 5f;
			case MAGICAL_ATTACK:
				return level * 20f;
			case PHYSICAL_DEFENSE:
				return level * 17f;
			case MAGICAL_ACCURACY:
				return level * 25f;
			case MAGICAL_RESIST: // formula with help of https://www.wolframalpha.com/input/?i=fit+(1,20),(15,270),(30,585),(50,1075),(60,1350),(65,1495)
				return 0.1f * (float) Math.pow(level, 2) + 16.5f * level;
			case PHYSICAL_ACCURACY:
				return level * 37f;
			case PARRY:
				return level * 40f;
			case PHYSICAL_CRITICAL_RESIST:
				return level * 2.2f;
			case ABNORMAL_RESISTANCE_ALL:
				return 100f;
			default:
				throw new IllegalArgumentException("Stat calculation for " + stat + " is not implemented");
		}
	}

	private static float getRatingModifier(StatEnum stat, NpcRating rating) {
		switch (rating) {
			case JUNK:
				switch (stat) {
					case MAGICAL_ATTACK:
						return 0.4f;
					case ABNORMAL_RESISTANCE_ALL:
						return 0f;
				}
				return 1f;
			case NORMAL:
				switch (stat) {
					case MAGICAL_ATTACK:
						return 0.4f;
					case ABNORMAL_RESISTANCE_ALL:
						return 0f;
				}
				return 1f;
			case ELITE:
				switch (stat) {
					case PHYSICAL_ATTACK:
						return 1.7f;
					case MAGICAL_ATTACK:
						return 0.5f;
					case MAGICAL_DEFEND:
					case PHYSICAL_DEFENSE:
						return 1.25f;
					case MAGICAL_RESIST:
						return 1.05f;
					case PHYSICAL_ACCURACY:
					case MAGICAL_ACCURACY:
						return 1.03f;
					case PARRY:
						return 1.025f;
					case PHYSICAL_CRITICAL_RESIST:
						return 1.5f;
					case ABNORMAL_RESISTANCE_ALL:
						return 5f;
				}
				return 1f;
			case HERO:
				switch (stat) {
					case PHYSICAL_ATTACK:
						return 2.4f;
					case MAGICAL_ATTACK:
						return 0.6f;
					case PHYSICAL_ACCURACY:
					case MAGICAL_ACCURACY:
						return 1.075f;
					case MAGICAL_RESIST:
						return 1.2f;
					case MAGICAL_DEFEND:
					case PHYSICAL_DEFENSE:
						return 1.5f;
					case PARRY:
						return 1.07f;
					case PHYSICAL_CRITICAL_RESIST:
						return 1.75f;
					case ABNORMAL_RESISTANCE_ALL:
						return 20f;
				}
				return 1f;
			case LEGENDARY:
				switch (stat) {
					case PHYSICAL_ATTACK:
						return 2.6f;
					case PHYSICAL_DEFENSE:
					case MAGICAL_DEFEND:
						return 1.75f;
					case MAGICAL_RESIST:
						return 1.35f;
					case MAGICAL_ACCURACY:
						return 1.47f;
					case MAGICAL_ATTACK:
					case PARRY:
					case PHYSICAL_ACCURACY:
						return 1.1f;
					case PHYSICAL_CRITICAL_RESIST:
						return 2.1f;
					case ABNORMAL_RESISTANCE_ALL:
						return 20f;
				}
				return 1f;
			default:
				throw new IllegalArgumentException("Stat calculation for npc rating " + rating + " is not implemented");
		}
	}

	private static float getRankModifier(StatEnum stat, NpcRank rank) {
		switch (rank) {
			case NOVICE:
				switch (stat) {
					case ABNORMAL_RESISTANCE_ALL:
						return 0.2f;
					default:
						return 1f;
				}
			case DISCIPLINED:
				switch (stat) {
					case PHYSICAL_ATTACK:
						return 1.2f;
					case MAGICAL_RESIST:
						return 1.02f;
					case MAGICAL_DEFEND:
					case PHYSICAL_DEFENSE:
						return 1.1f;
					case MAGICAL_ATTACK:
						return 1.45f;
					case PARRY:
						return 1.05f;
					case PHYSICAL_CRITICAL_RESIST:
						return 1.2f;
					case ABNORMAL_RESISTANCE_ALL:
						return 0.4f;
				}
				return 1f;
			case SEASONED:
				switch (stat) {
					case PHYSICAL_ATTACK:
						return 1.6f;
					case MAGICAL_DEFEND:
					case PHYSICAL_DEFENSE:
						return 1.2f;
					case MAGICAL_RESIST:
						return 1.03f;
					case MAGICAL_ATTACK:
						return 1.45f;
					case PARRY:
						return 1.1f;
					case PHYSICAL_ACCURACY:
					case MAGICAL_ACCURACY:
						return 1.01f;
					case PHYSICAL_CRITICAL_RESIST:
						return 1.4f;
					case ABNORMAL_RESISTANCE_ALL:
						return 0.6f;
				}
				return 1f;
			case EXPERT:
				switch (stat) {
					case PHYSICAL_ATTACK:
						return 1.65f;
					case MAGICAL_RESIST:
						return 1.04f;
					case MAGICAL_DEFEND:
					case PHYSICAL_DEFENSE:
						return 1.3f;
					case MAGICAL_ATTACK:
						return 1.7f;
					case PARRY:
						return 1.1f;
					case PHYSICAL_ACCURACY:
					case MAGICAL_ACCURACY:
						return 1.02f;
					case PHYSICAL_CRITICAL_RESIST:
						return 1.6f;
					case ABNORMAL_RESISTANCE_ALL:
						return 1f;
				}
				return 1f;
			case VETERAN:
				switch (stat) {
					case PHYSICAL_ATTACK:
						return 1.7f;
					case MAGICAL_DEFEND:
					case PHYSICAL_DEFENSE:
						return 1.4f;
					case MAGICAL_RESIST:
						return 1.05f;
					case MAGICAL_ATTACK:
						return 1.7f;
					case PARRY:
						return 1.12f;
					case PHYSICAL_ACCURACY:
					case MAGICAL_ACCURACY:
						return 1.03f;
					case PHYSICAL_CRITICAL_RESIST:
						return 1.8f;
					case ABNORMAL_RESISTANCE_ALL:
						return 1.4f;
				}
				return 1f;
			case MASTER:
				switch (stat) {
					case PHYSICAL_ATTACK:
						return 1.85f;
					case MAGICAL_DEFEND:
					case PHYSICAL_DEFENSE:
						return 1.5f;
					case MAGICAL_RESIST:
						return 1.06f;
					case MAGICAL_ATTACK:
						return 1.7f;
					case PARRY:
						return 1.12f;
					case PHYSICAL_ACCURACY:
					case MAGICAL_ACCURACY:
						return 1.04f;
					case PHYSICAL_CRITICAL_RESIST:
						return 2f;
					case ABNORMAL_RESISTANCE_ALL:
						return 1.7f;
				}
				return 1f;
			default:
				throw new IllegalArgumentException("Stat calculation for npc rank " + rank + " is not implemented");
		}
	}
}
