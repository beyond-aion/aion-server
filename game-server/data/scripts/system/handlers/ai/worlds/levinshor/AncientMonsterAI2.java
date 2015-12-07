package ai.worlds.levinshor;

import java.util.concurrent.Future;

import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.ai2.handler.TargetEventHandler;
import com.aionemu.gameserver.utils.ThreadPoolManager;

import ai.AggressiveNpcAI2;

/**
 * @author Yeats
 *
 */
@AIName("LDF4_Advance_Ancient_Monster")
public class AncientMonsterAI2 extends AggressiveNpcAI2 {

	private Future<?> despawnTask;
	
	@Override
	protected void handleSpawned() {
		super.handleSpawned();
		despawn();
	}
	
	@Override
	protected void handleMoveArrived() {
		super.handleMoveArrived();
		if (getOwner().getDistanceToSpawnLocation() > 15) {
			TargetEventHandler.onTargetGiveup(this);
		}
	}
	
	@Override
	protected void handleDied() {
		super.handleDied();
		cancelTask();
	}
	
	private void despawn() {
		despawnTask = ThreadPoolManager.getInstance().schedule(new Runnable() {
			@Override
			public void run() {
				if (getOwner() != null && !isAlreadyDead()) {
					getOwner().getController().onDelete();
				}
			}
		}, 1000 * 60 * 60);
	}
	
	private void cancelTask() {
		if (despawnTask != null && !despawnTask.isCancelled()) {
			despawnTask.cancel(true);
		}
	}
}
