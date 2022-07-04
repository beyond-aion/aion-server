package com.aionemu.gameserver.model.stats.container;

import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.stats.calc.Stat2;
import com.aionemu.gameserver.utils.stats.CalculationType;

/**
 * @author ATracer
 */
public class SummonedObjectGameStats extends NpcGameStats {

	public SummonedObjectGameStats(Npc owner) {
		super(owner);
	}

	@Override
	public Stat2 getStat(StatEnum statEnum, float base, CalculationType... calculationTypes) {
		Stat2 stat = super.getStat(statEnum, base, calculationTypes);
		if (owner.getMaster() == null)
			return stat;
		switch (statEnum) {
			case MAGICAL_ATTACK, MAGICAL_ACCURACY, MAGICAL_RESIST -> {
				stat.setBonusRate(0.2f);
				return owner.getMaster().getGameStats().getItemStatBoost(statEnum, stat);
			}
			case PHYSICAL_ACCURACY -> {
				stat.setBonusRate(0.2f);
				owner.getMaster().getGameStats().getItemStatBoost(StatEnum.MAIN_HAND_ACCURACY, stat);
				return owner.getMaster().getGameStats().getItemStatBoost(statEnum, stat);
			}
			case PHYSICAL_ATTACK -> {
				stat.setBonusRate(0.2f);
				owner.getMaster().getGameStats().getItemStatBoost(StatEnum.MAIN_HAND_POWER, stat);
				return owner.getMaster().getGameStats().getItemStatBoost(statEnum, stat);
			}
		}
		return stat;
	}

	@Override
	public Stat2 getMBoost() {
		return getStat(StatEnum.BOOST_MAGICAL_SKILL, (int) (owner.getMaster().getGameStats().getMBoost().getCurrent() * 0.6f));
	}
}
