package com.aionemu.gameserver.model.stats.container;

import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.stats.calc.Stat2;
import com.aionemu.gameserver.utils.stats.CalculationType;

/**
 * @author Yeats
 */
public class ServantGameStats extends SummonedObjectGameStats {

	private int fixedMBoost;
	private int fixedHealBoost;
	private int fixedMagicalAccuracy;

	public ServantGameStats(Npc owner) {
		super(owner);
	}

	@Override
	public Stat2 getStat(StatEnum statEnum, float base, CalculationType... calculationTypes) {
		return super.getStat(statEnum, statEnum == StatEnum.HEAL_BOOST ? fixedHealBoost : base, calculationTypes);
	}

	@Override
	public Stat2 getMBoost() {
		return getStat(StatEnum.BOOST_MAGICAL_SKILL, fixedMBoost);
	}

	@Override
	public Stat2 getMAccuracy() {
		return getStat(StatEnum.MAGICAL_ACCURACY, fixedMagicalAccuracy);
	}

	// TODO: there might be more stats which are set only at spawn
	public void setUpStats() {
		setFixedMBoost();
		setFixedHealBoost();
		setFixedMagicalAccuracy();
	}

	private void setFixedMBoost() {
		fixedMBoost = owner.getMaster().getGameStats().getMBoost().getBonus();
	}

	private void setFixedHealBoost() {
		Stat2 healBoostStat = super.getStat(StatEnum.HEAL_BOOST, 0);
		healBoostStat.setBonusRate(0.5f);
		fixedHealBoost = owner.getMaster().getGameStats().getItemStatBoost(StatEnum.HEAL_BOOST, healBoostStat).getCurrent();
		if (fixedHealBoost > 500) {
			fixedHealBoost = 500;
		}
	}

	private void setFixedMagicalAccuracy() {
		Stat2 magicalAccuracyStat = super.getStat(StatEnum.MAGICAL_ACCURACY, getStatsTemplate().getMacc());
		magicalAccuracyStat.setBaseRate(1.2f);
		fixedMagicalAccuracy = owner.getMaster().getGameStats().getItemStatBoost(StatEnum.MAGICAL_ACCURACY, magicalAccuracyStat).getCurrent();
	}
}
