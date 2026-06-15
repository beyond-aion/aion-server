package com.aionemu.gameserver.model.base;

import com.aionemu.commons.utils.Rnd;

/**
 * @author Estrayl
 */
public class CasualBase extends Base<BaseLocation> {

	public CasualBase(BaseLocation bLoc) {
		super(bLoc);
	}

	@Override
	protected int getAssaultDelay() {
		if (getWorldId() == 600100000)
			return Rnd.get(300, 1200) * 6000;
		return Rnd.get(75, 200) * 6000;
	}

	@Override
	protected int getAssaultDespawnDelay() {
		return 15 * 60000;
	}

	@Override
	protected int getBossSpawnDelay() {
		if (getWorldId() == 600090000)
			return 0;
		return Rnd.get(100, 200) * 6000;
	}

	@Override
	protected int getNpcSpawnDelay() {
		return Rnd.get(60, 295) * 1000;
	}

}
