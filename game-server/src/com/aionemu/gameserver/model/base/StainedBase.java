package com.aionemu.gameserver.model.base;

import java.util.concurrent.Future;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.spawnengine.SpawnHandlerType;
import com.aionemu.gameserver.utils.ThreadPoolManager;

/**
 * @author Estrayl
 */
public class StainedBase extends Base<StainedBaseLocation> {

	private Future<?> enhancedSpawnTask;

	public StainedBase(StainedBaseLocation bLoc) {
		super(bLoc);
	}

	@Override
	protected void handleStop() {
		cancelTask(enhancedSpawnTask);
		super.handleStop();
	}

	public void scheduleEnhancedSpawns() {
		enhancedSpawnTask = ThreadPoolManager.getInstance().schedule(() -> {
			if (isStopped())
				return;
			despawnByHandlerType(SpawnHandlerType.GUARDIAN); // prevents double spawns
			despawnByHandlerType(SpawnHandlerType.OUTRIDER_ENHANCED);
			spawnBySpawnHandler(SpawnHandlerType.GUARDIAN, getOccupier());
			spawnBySpawnHandler(SpawnHandlerType.OUTRIDER_ENHANCED, getOccupier());
		}, 295 * 1000);
	}

	public void deactivateEnhancedSpawns() {
		cancelTask(enhancedSpawnTask);
		despawnByHandlerType(SpawnHandlerType.GUARDIAN);
		despawnByHandlerType(SpawnHandlerType.OUTRIDER_ENHANCED);
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
		return 30 * 1000;
	}

	public BaseColorType getColor() {
		return getLocation().getColor();
	}
}
