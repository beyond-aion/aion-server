package ai.instance.idgelDome;

import java.util.concurrent.Future;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.NpcAI;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.utils.ThreadPoolManager;

/**
 * @author Ritsu
 */
@AIName("repelling_flame_cannon")
public class RepellingFlameCannonAI extends NpcAI {

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
				if (isDead()) {
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
