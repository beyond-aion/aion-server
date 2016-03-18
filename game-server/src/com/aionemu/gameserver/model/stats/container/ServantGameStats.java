package com.aionemu.gameserver.model.stats.container;

import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.stats.calc.Stat2;

/**
 * @author Yeats 18.03.2016.
 */
public class ServantGameStats extends SummonedObjectGameStats {

	private int fixedMBoost;

	//TODO other stats might have a fixed value too
	@Override
	public Stat2 getStat(StatEnum statEnum, int base) {
		Stat2 stat = super.getStat(statEnum, base);
		if (owner.getMaster() == null)
			return stat;
		switch (statEnum) {
			case MAGICAL_ATTACK:
			case MAGICAL_ACCURACY:
			case MAGICAL_RESIST:
				stat.setBonusRate(0.2f);
				return owner.getMaster().getGameStats().getItemStatBoost(statEnum, stat);
			case PHYSICAL_ACCURACY:
				stat.setBonusRate(0.2f);
				owner.getMaster().getGameStats().getItemStatBoost(StatEnum.MAIN_HAND_ACCURACY, stat);
				return owner.getMaster().getGameStats().getItemStatBoost(statEnum, stat);
			case PHYSICAL_ATTACK:
				stat.setBonusRate(0.2f);
				owner.getMaster().getGameStats().getItemStatBoost(StatEnum.MAIN_HAND_POWER, stat);
				return owner.getMaster().getGameStats().getItemStatBoost(statEnum, stat);
			case PVE_ATTACK_RATIO:
				stat.setBonus(150); // equals 15% pve bonus
				return stat;
		}
		return stat;
	}

	public ServantGameStats(Npc owner) {
		super(owner);
	}


	@Override
	public Stat2 getMBoost() {
		return getStat(StatEnum.BOOST_MAGICAL_SKILL, fixedMBoost);
	}

	// TODO: there might be more stats which are set only at spawn
	public void setUpStats() {
		fixedMBoost = owner.getMaster().getGameStats().getMBoost().getBonus();
	}
}
