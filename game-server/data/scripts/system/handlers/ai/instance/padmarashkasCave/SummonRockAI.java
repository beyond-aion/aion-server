package ai.instance.padmarashkasCave;

import com.aionemu.gameserver.ai.AIActions;
import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.utils.ThreadPoolManager;

import ai.AggressiveNpcAI;

/**
 * @author Ritsu
 */
@AIName("summonrock")
public class SummonRockAI extends AggressiveNpcAI {

	@Override
	protected void handleSpawned() {
		super.handleSpawned();
		doSchedule();
	}

	private void useSkill() {
		AIActions.targetSelf(this);
		AIActions.useSkill(this, 19180);
		doSchedule();
	}

	private void doSchedule() {
		ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				if (!isAlreadyDead()) {
					useSkill();
				}
			}

		}, 5000);
	}
}
