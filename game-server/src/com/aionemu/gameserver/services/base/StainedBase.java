package com.aionemu.gameserver.services.base;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.model.base.BaseColorType;
import com.aionemu.gameserver.model.base.StainedBaseLocation;


/**
 * @author Estrayl
 *
 */
public class StainedBase extends Base<StainedBaseLocation> {

	public StainedBase(StainedBaseLocation bLoc) {
		super(bLoc);
	}

	@Override
	protected int getAssaultDelay() {
		return Rnd.get(300, 1200) * 6000;
	}

	@Override
	protected int getAssaultDespawnDelay() {
		return Rnd.get(100, 150) * 6000;
	}

	@Override
	protected int getBossSpawnDelay() {
		return Rnd.get(100, 200) * 6000;
	}

	@Override
	protected int getNpcSpawnDelay() {
		return 295;
	}
	
	public BaseColorType getColor() {
		return getLocation().getColor();
	}

	public boolean isEnhanced() {
		return getLocation().isEnhanced();
	}
	
	public void setEnhanced(boolean isEnhanced) {
		getLocation().setEnhanced(isEnhanced);
	}
}
