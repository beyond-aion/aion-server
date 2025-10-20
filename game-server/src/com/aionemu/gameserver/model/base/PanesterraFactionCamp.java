package com.aionemu.gameserver.model.base;

import com.aionemu.gameserver.model.gameobjects.Creature;

/**
 * @author Estrayl
 */
public class PanesterraFactionCamp extends PanesterraBase {

	public PanesterraFactionCamp(PanesterraBaseLocation loc) {
		super(loc);
	}

	@Override
	protected int getBossSpawnDelay() {
		return 10 * 60000;
	}

	@Override
	protected int getNpcSpawnDelay() {
		return 10 * 60000; // Retail delay
	}

	@Override
	public BaseOccupier getOccupier(Creature bossKiller) {
		return BaseOccupier.PEACE; // If the soul anchor (boss) is destroyed, the camp will be eliminated
	}
}
