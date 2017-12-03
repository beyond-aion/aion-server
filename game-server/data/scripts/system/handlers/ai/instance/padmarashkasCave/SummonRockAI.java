package ai.instance.padmarashkasCave;

import com.aionemu.gameserver.ai.AIActions;
import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.utils.ThreadPoolManager;

import ai.AggressiveNpcAI;

/**
 * @author Ritsu
 */
@AIName("summonrock")
public class SummonRockAI extends AggressiveNpcAI {

	public SummonRockAI(Npc owner) {
		super(owner);
	}

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
				if (!isDead()) {
					useSkill();
				}
			}

		}, 5000);
	}
}
