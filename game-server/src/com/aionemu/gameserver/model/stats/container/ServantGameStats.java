package com.aionemu.gameserver.model.stats.container;

import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.stats.calc.Stat2;
import com.aionemu.gameserver.utils.stats.CalculationType;

/**
 * @author Yeats 18.03.2016.
 */
public class ServantGameStats extends SummonedObjectGameStats {

	private int fixedMBoost;
	private int fixedHealBoost;

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

	// TODO: there might be more stats which are set only at spawn
	public void setUpStats() {
		fixedMBoost = owner.getMaster().getGameStats().getMBoost().getBonus();
		Stat2 healBoostStat = super.getStat(StatEnum.HEAL_BOOST, 0);
		healBoostStat.setBonusRate(0.5f);
		fixedHealBoost = owner.getMaster().getGameStats().getItemStatBoost(StatEnum.HEAL_BOOST, healBoostStat).getCurrent();
		if (fixedHealBoost > 500) {
			fixedHealBoost = 500;
		}
	}
}
