package ai.instance.empyreanCrucible;

import com.aionemu.gameserver.ai.AIActions;
import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.utils.ThreadPoolManager;

import ai.GeneralNpcAI;

/**
 * @author Luzien, w4terbomb
 */
@AIName("spark_of_darkness")
public class SparkOfDarknessAI extends GeneralNpcAI {

	public SparkOfDarknessAI(Npc owner) {
		super(owner);
	}

	@Override
	protected void handleSpawned() {
		super.handleSpawned();
		startEventTask();
		startLifeTask();
	}

	private void startEventTask() {
		ThreadPoolManager.getInstance().schedule(() -> {
			if (!isDead())
				getOwner().queueSkill(19554, 1);
		}, 500);
	}

	private void startLifeTask() {
		ThreadPoolManager.getInstance().schedule(() -> AIActions.deleteOwner(SparkOfDarknessAI.this), 6500);
	}

	@Override
	public boolean isMoveSupported() {
		return false;
	}
}
