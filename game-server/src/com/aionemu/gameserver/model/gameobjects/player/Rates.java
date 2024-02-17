package com.aionemu.gameserver.model.gameobjects.player;

import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.configs.main.RatesConfig;
import com.aionemu.gameserver.model.stats.container.StatEnum;

/**
 * @author antness, Neon
 */
public enum Rates {

	XP_HUNTING {

		@Override
		public long calcResult(Player player, long xp) {
			return (long) Math.min(xp * calcXpRate(player, RatesConfig.XP_SOLO_RATES, StatEnum.BOOST_HUNTING_XP_RATE),
				player.getCommonData().getExpNeed() * 0.2f);
		}
	},
	XP_GROUP_HUNTING {

		@Override
		public long calcResult(Player player, long xp) {
			return (long) Math.min(xp * calcXpRate(player, RatesConfig.XP_GROUP_RATES, StatEnum.BOOST_GROUP_HUNTING_XP_RATE),
				player.getCommonData().getExpNeed() * 0.2f);
		}
	},
	XP_QUEST {

		@Override
		public long calcResult(Player player, long xp) {
			return (long) (xp * calcXpRate(player, RatesConfig.XP_QUEST_RATES, StatEnum.BOOST_QUEST_XP_RATE));
		}
	},
	XP_GATHERING {

		@Override
		public long calcResult(Player player, long xp) {
			return (long) (xp * calcXpRate(player, RatesConfig.XP_GATHERING_RATES, StatEnum.BOOST_GATHERING_XP_RATE));
		}
	},
	XP_CRAFTING {

		@Override
		public long calcResult(Player player, long xp) {
			return (long) (xp * calcXpRate(player, RatesConfig.XP_CRAFTING_RATES, StatEnum.BOOST_CRAFTING_XP_RATE));
		}
	},
	XP_PVP {

		@Override
		public long calcResult(Player player, long xp) {
			return (long) (xp * get(player, RatesConfig.XP_PVP_RATES));
		}
	},
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

	public abstract long calcResult(Player player, long value);

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

	private static float calcXpRate(Player player, float[] membershipRates, StatEnum boostRate) {
		float endRate = get(player, membershipRates);
		endRate *= player.getGameStats().getStat(boostRate, 100).getCurrent() / 100f;
		if (player.isLegionMember() && player.getLegion().hasBonus())
			endRate *= 1.1f;
		return endRate;
	}
}
