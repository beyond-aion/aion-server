package com.aionemu.gameserver.model.base;

/**
 * @author Estrayl
 *         TODO: Implement Anoha/Wealhtheow features!
 */
public class SiegeBase extends Base<SiegeBaseLocation> {

	public SiegeBase(SiegeBaseLocation bLoc) {
		super(bLoc);
	}

	@Override
	protected int getAssaultDelay() {
		return 180 * 60000;
	}

	@Override
	protected int getAssaultDespawnDelay() {
		return 295 * 1000; // TODO: Dont Reassault with Despawntask
	}

	@Override
	protected int getBossSpawnDelay() {
		return 0;
	}

	@Override
	protected int getNpcSpawnDelay() {
		return 0;
	}

}
