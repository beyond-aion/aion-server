package ai.instance.padmarashkasCave;

import com.aionemu.gameserver.ai2.AI2Actions;
import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.utils.ThreadPoolManager;

import ai.AggressiveNpcAI2;

/**
 * @author Ritsu
 */
@AIName("summonrock")
public class SummonRockAI2 extends AggressiveNpcAI2 {

	@Override
	protected void handleSpawned() {
		super.handleSpawned();
		doSchedule();
	}

	private void useSkill() {
		AI2Actions.targetSelf(this);
		AI2Actions.useSkill(this, 19180);
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
