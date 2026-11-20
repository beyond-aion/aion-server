package com.aionemu.gameserver.model.gameobjects.player;

import java.util.function.Supplier;

import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.configs.main.RatesConfig;
import com.aionemu.gameserver.model.stats.container.StatEnum;

/**
 * @author antness, Neon
 */
public enum Rates {

	XP_HUNTING(StatEnum.BOOST_HUNTING_XP_RATE, true, true, () -> RatesConfig.XP_SOLO_RATES),
	XP_GROUP_HUNTING(StatEnum.BOOST_GROUP_HUNTING_XP_RATE, true, true, () -> RatesConfig.XP_GROUP_RATES),
	/**
	 * XP boost effects do not apply to quests, as in retail.<br>
	 * Not retail: the quest XP rates and the legion bonus apply to quests here. Retail gives quest XP (type 2 of User::GetExperience) without the
	 * server XP multiplier and without the legion bonus. To match retail, use rates of 1 for rates.xp.quest and pass false for the legion bonus.
	 */
	XP_QUEST(null, false, true, () -> RatesConfig.XP_QUEST_RATES),
	XP_GATHERING(StatEnum.BOOST_GATHERING_XP_RATE, true, true, () -> RatesConfig.XP_GATHERING_RATES),
	XP_CRAFTING(StatEnum.BOOST_CRAFTING_XP_RATE, true, true, () -> RatesConfig.XP_CRAFTING_RATES),
	XP_PVP(StatEnum.BOOST_HUNTING_XP_RATE, true, true, () -> RatesConfig.XP_PVP_RATES),
	SKILL_XP_GATHERING {

		@Override
		public long calcResult(Player player, long skillXp) {
			return (long) (skillXp * get(player, RatesConfig.SKILL_XP_GATHERING_RATES));
		}
	},
	SKILL_XP_CRAFTING {

		@Override
		public long calcResult(Player player, long skillXp) {
			return (long) (skillXp * get(player, RatesConfig.SKILL_XP_CRAFTING_RATES));
		}
	},
	AP_PVP {

		@Override
		public long calcResult(Player player, long ap) {
			float statRate = player.getGameStats().getStat(StatEnum.AP_BOOST, 100).getCurrent() / 100f;
			return (long) (ap * get(player, RatesConfig.AP_PVP_RATES) * statRate);
		}
	},
	AP_PVP_LOST {

		@Override
		public long calcResult(Player player, long ap) {
			return (long) (ap * get(player, RatesConfig.AP_PVP_LOSS_RATES));
		}
	},
	AP_PVE {

		@Override
		public long calcResult(Player player, long ap) {
			float statRate = player.getGameStats().getStat(StatEnum.AP_BOOST, 100).getCurrent() / 100f;
			return (long) (ap * get(player, RatesConfig.AP_PVE_RATES) * statRate);
		}
	},
	AP_QUEST {

		@Override
		public long calcResult(Player player, long ap) {
			return (long) (ap * get(player, RatesConfig.AP_QUEST_RATES));
		}
	},
	AP_DREDGION {

		@Override
		public long calcResult(Player player, long ap) {
			return (long) (ap * get(player, RatesConfig.AP_DREDGION_RATES));
		}
	},
	GP {

		@Override
		public long calcResult(Player player, long gp) {
			return (long) (gp * get(player, RatesConfig.GP_RATES));
		}
	},
	DP_PVE {

		@Override
		public long calcResult(Player player, long dp) {
			return (long) (dp * get(player, RatesConfig.DP_PVE_RATES));
		}
	},
	DP_PVP {

		@Override
		public long calcResult(Player player, long dp) {
			return (long) (dp * get(player, RatesConfig.DP_PVP_RATES));
		}
	},
	QUEST_KINAH {

		@Override
		public long calcResult(Player player, long kinah) {
			return (long) (kinah * get(player, RatesConfig.QUEST_KINAH_RATES));
		}
	},
	GATHERING_COUNT {

		@Override
		public long calcResult(Player player, long gatherCount) {
			return (long) (gatherCount * get(player, RatesConfig.GATHERING_COUNT_RATES));
		}
	},
	SELL_LIMIT {

		@Override
		public long calcResult(Player player, long sellLimit) {
			return (long) (sellLimit * get(player, RatesConfig.SELL_LIMIT_RATES));
		}
	};

	private static final int LEGION_BONUS_PERCENT = 10;

	private final StatEnum xpBoostStat;
	private final boolean limitsXp;
	private final boolean legionBonus;
	private final Supplier<float[]> xpRates;

	Rates() {
		this(null, false, false, null);
	}

	Rates(StatEnum xpBoostStat, boolean limitsXp, boolean legionBonus, Supplier<float[]> xpRates) {
		this.xpBoostStat = xpBoostStat;
		this.limitsXp = limitsXp;
		this.legionBonus = legionBonus;
		this.xpRates = xpRates;
	}

	/**
	 * XP rates apply {@link #calcXpWithoutLimit(Player, long)}, then a single hunting, PvP, gathering or crafting reward gives at most 20% of the XP
	 * needed for the player's current level. The other rates override this method.
	 */
	public long calcResult(Player player, long value) {
		long xp = calcXpWithoutLimit(player, value);
		return limitsXp ? Math.min(xp, player.getCommonData().getExpNeed() / 5) : xp;
	}

	/**
	 * @return The XP multiplied by the membership rate, without the limit of {@link #calcResult(Player, long)}.
	 */
	public long calcXpWithoutLimit(Player player, long xp) {
		if (xpRates == null)
			throw new UnsupportedOperationException(name() + " is not an XP rate");
		return (long) (xp * get(player, xpRates.get()));
	}

	/**
	 * The legion bonus adds a share of the XP before Energy of Repose and Salvation, like the retail LegionBonusBuff of conditional_bonus_attr.xml.
	 * Retail gives it for gathering, crafting, kills and PvP.<br>
	 * Not retail: quests get it here too, see {@link #XP_QUEST}.
	 *
	 * @return The XP the legion bonus adds to the given XP, before rates.
	 */
	public long getLegionBonus(Player player, long xp) {
		if (!legionBonus || !player.isLegionMember() || !player.getLegion().hasBonus())
			return 0;
		return xp * LEGION_BONUS_PERCENT / 100;
	}

	/**
	 * XP boost effects increase the XP before Energy of Repose and Energy of Salvation add their share, so a boosted reward also uses up more
	 * energy.
	 * 
	 * @return The XP increased by the player's XP boost effects, before rates.
	 */
	public long applyXpBoost(Player player, long xp) {
		if (xpBoostStat == null)
			return xp;
		return (long) (xp * (player.getGameStats().getStat(xpBoostStat, 100).getCurrent() / 100f));
	}

	public int calcResult(Player player, int value) {
		long result = calcResult(player, (long) value);
		try {
			return Math.toIntExact(result);
		} catch (ArithmeticException e) {
			LoggerFactory.getLogger(getClass()).error("{} result is too large for {}: {}", name(), player, result, e);
			return value;
		}
	}

	/**
	 * @return The rate for the given player, selected by his current membership
	 */
	public static float get(Player player, float[] membershipRates) {
		if (membershipRates.length == 0) {
			LoggerFactory.getLogger(Rates.class).warn("Missing rates", new IllegalStateException());
			return 1;
		}
		int membershipLevel = player.getAccount().getMembership();
		return membershipRates[Math.min(membershipRates.length - 1, membershipLevel)];
	}
}
