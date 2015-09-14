package ai.instance.idgelDome;

import java.util.concurrent.Future;

import com.aionemu.commons.network.util.ThreadPoolManager;
import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.ai2.NpcAI2;
import com.aionemu.gameserver.skillengine.SkillEngine;

/**
 * @author Ritsu
 */
@AIName("unstable_id_energy")
public class UnstableIdeEnergyAI2 extends NpcAI2 {

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
					SkillEngine.getInstance().getSkill(getOwner(), 21559, 65, getOwner()).useWithoutPropSkill();
				}
			}
		}, Rnd.get(10000, 30000), Rnd.get(10000, 30000));
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
