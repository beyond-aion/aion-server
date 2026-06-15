package com.aionemu.gameserver.model.base;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.player.Player;

/**
 * @author Estrayl
 */
public class PanesterraBase extends Base<PanesterraBaseLocation> {

	public PanesterraBase(PanesterraBaseLocation loc) {
		super(loc);
	}

	@Override
	protected int getAssaultDelay() {
		return Rnd.get(75, 200) * 6000;
	}

	@Override
	protected int getAssaultDespawnDelay() {
		return 15 * 60000; // Retail delay
	}

	@Override
	protected int getBossSpawnDelay() {
		return 20 * 60000; // Retail delay
	}

	@Override
	protected int getNpcSpawnDelay() {
		return 5 * 60000; // Retail delay
	}

	@Override
	protected BaseOccupier chooseAssaultRace() {
		return BaseOccupier.BALAUR;
	}

	@Override
	public BaseOccupier getOccupier(Creature bossKiller) {
		if (bossKiller instanceof Player player && player.getPanesterraFaction() != null)
			return BaseOccupier.findBy(player.getPanesterraFaction());
		return getLocation().getTemplate().getDefaultOccupier();
	}
}
