package ai.instance.sauroBase;

import java.util.concurrent.Future;

import com.aionemu.gameserver.ai2.AI2Actions;
import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.ai2.NpcAI2;
import com.aionemu.gameserver.utils.ThreadPoolManager;

@AIName("danuar")
public class GuardDanuarAI2 extends NpcAI2 {

	private Future<?> skillTask;

	@Override
	protected void handleSpawned() {
		super.handleSpawned();
		startpower();
	}

	private void startpower() {
		skillTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(new Runnable() {

			@Override
			public void run() {
				AI2Actions.targetSelf(GuardDanuarAI2.this);
				AI2Actions.useSkill(GuardDanuarAI2.this, 21185);
			}
		}, 3000, 5000);
	}

	private void cancelskillTask() {
		if (skillTask != null && !skillTask.isCancelled()) {
			skillTask.cancel(true);
		}
	}

	@Override
	protected void handleDied() {
		cancelskillTask();
		super.handleDied();
	}

	@Override
	protected void handleDespawned() {
		cancelskillTask();
		super.handleDespawned();
	}
}
