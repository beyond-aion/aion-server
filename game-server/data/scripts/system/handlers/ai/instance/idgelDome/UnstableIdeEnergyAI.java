package ai.instance.idgelDome;

import java.util.concurrent.Future;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.NpcAI;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.utils.ThreadPoolManager;

/**
 * @author Ritsu
 */
@AIName("unstable_id_energy")
public class UnstableIdeEnergyAI extends NpcAI {

	private Future<?> skillTask = null;

	public UnstableIdeEnergyAI(Npc owner) {
		super(owner);
	}

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
