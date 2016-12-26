package com.aionemu.gameserver.model.stats.calc;

import com.aionemu.gameserver.model.stats.container.StatEnum;
import com.aionemu.gameserver.model.templates.npc.NpcRank;
import com.aionemu.gameserver.model.templates.npc.NpcRating;

/**
 * @author Estrayl
 * @modified Neon
 */
public class NpcStatCalculation {

	public static int calculateStat(StatEnum stat, NpcRating rating, NpcRank rank, int level) {
		float value = getBaseValue(stat);
		float ratingMod = getRatingModifier(stat, rating);
		float rankMod = getRankModifier(stat, rank);
		return Math.round(value * level * ratingMod * rankMod);
	}

	private static float getBaseValue(StatEnum stat) {
		switch (stat) {
			case PHYSICAL_ATTACK:
				return 8f;
			case MAGICAL_ATTACK:
			case PHYSICAL_DEFENSE:
				return 20f;
			case MAGICAL_ACCURACY:
				return 25f;
			case MAGICAL_RESIST:
				return 23f;
			case PHYSICAL_ACCURACY:
				return 37f;
			case PARRY:
				return 40f;
			case PHYSICAL_CRITICAL_RESIST:
				return 2.2f;
			default:
				return 0;
		}
	}

	private static float getRatingModifier(StatEnum stat, NpcRating rating) {
		switch (rating) {
			case JUNK:
				switch (stat) {
					case PHYSICAL_ATTACK:
						return 1.05f;
					case MAGICAL_ATTACK:
						return 0.4f;
				}
				return 1f;
			case NORMAL:
				switch (stat) {
					case PHYSICAL_ATTACK:
						return 1.05f;
					case MAGICAL_ATTACK:
						return 0.4f;
				}
				return 1f;
			case ELITE:
				switch (stat) {
					case PHYSICAL_ATTACK:
						return 1.1f;
					case MAGICAL_ATTACK:
						return 0.5f;
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
				}
				return 1f;
			case HERO:
				switch (stat) {
					case PHYSICAL_ATTACK:
						return 1.4f;
					case MAGICAL_ATTACK:
						return 0.6f;
					case PHYSICAL_ACCURACY:
					case MAGICAL_ACCURACY:
						return 1.075f;
					case MAGICAL_RESIST:
						return 1.2f;
					case PHYSICAL_DEFENSE:
						return 1.5f;
					case PARRY:
						return 1.07f;
					case PHYSICAL_CRITICAL_RESIST:
						return 1.75f;
				}
				return 1f;
			case LEGENDARY:
				switch (stat) {
					case PHYSICAL_ATTACK:
					case PHYSICAL_DEFENSE:
						return 1.75f;
					case MAGICAL_RESIST:
						return 1.35f;
					case MAGICAL_ACCURACY:
					case MAGICAL_ATTACK:
					case PARRY:
					case PHYSICAL_ACCURACY:
						return 1.1f;
					case PHYSICAL_CRITICAL_RESIST:
						return 2.1f;
				}
				return 1f;
			default:
				return 0;
		}
	}

	private static float getRankModifier(StatEnum stat, NpcRank rank) {
		switch (rank) {
			case NOVICE:
				return 1f;
			case DISCIPLINED:
				switch (stat) {
					case PHYSICAL_ATTACK:
						return 1.1f;
					case MAGICAL_RESIST:
						return 1.02f;
					case PHYSICAL_DEFENSE:
						return 1.1f;
					case MAGICAL_ATTACK:
						return 1.45f;
					case PARRY:
						return 1.05f;
					case PHYSICAL_CRITICAL_RESIST:
						return 1.2f;
				}
				return 1f;
			case SEASONED:
				switch (stat) {
					case PHYSICAL_ATTACK:
						return 1.6f;
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
				}
				return 1f;
			case EXPERT:
				switch (stat) {
					case PHYSICAL_ATTACK:
						return 1.95f;
					case MAGICAL_RESIST:
						return 1.04f;
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
				}
				return 1f;
			case VETERAN:
				switch (stat) {
					case PHYSICAL_ATTACK:
						return 2.2f;
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
				}
				return 1f;
			case MASTER:
				switch (stat) {
					case PHYSICAL_ATTACK:
						return 2.5f;
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
				}
				return 1f;
			default:
				return 0;
		}
	}
}
