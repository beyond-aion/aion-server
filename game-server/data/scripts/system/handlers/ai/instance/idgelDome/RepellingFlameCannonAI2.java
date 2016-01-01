package ai.instance.idgelDome;

import java.util.concurrent.Future;

import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.ai2.NpcAI2;
import com.aionemu.gameserver.skillengine.SkillEngine;

/**
 * @author Ritsu
 */
@AIName("repelling_flame_cannon")
public class RepellingFlameCannonAI2 extends NpcAI2 {

	private Future<?> skillTask = null;

	@Override
	protected void handleSpawned() {
		super.handleSpawned();
		startTask();
	}

	private void startTask() {
		skillTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(new Runnable() {

			@Override
			public void run() {
				if (isAlreadyDead()) {
					cancelTask();
				} else {
					SkillEngine.getInstance().getSkill(getOwner(), 21648, 1, getOwner()).useNoAnimationSkill();
				}
			}
		}, 100, 100);
	}

	private void cancelTask() {
		if (skillTask != null && !skillTask.isDone()) {
			skillTask.cancel(true);
		}
	}

	@Override
	protected void handleDespawned() {
		cancelTask();
		super.handleDespawned();
	}
}
