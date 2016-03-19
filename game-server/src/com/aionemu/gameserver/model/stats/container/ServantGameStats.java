package com.aionemu.gameserver.model.stats.container;

import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.stats.calc.Stat2;

/**
 * @author Yeats 18.03.2016.
 */
public class ServantGameStats extends SummonedObjectGameStats {

	private int fixedMBoost;

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
