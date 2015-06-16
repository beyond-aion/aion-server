package ai.instance.rentusBase;

import ai.GeneralNpcAI2;

import com.aionemu.commons.network.util.ThreadPoolManager;
import com.aionemu.gameserver.ai2.AI2Actions;
import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.skillengine.SkillEngine;

/**
 *
 * @author xTz
 */
@AIName("spilled_oil")
public class SpilledOilAI2 extends GeneralNpcAI2 {

	private int count;

	@Override
	protected void handleSpawned() {
		super.handleSpawned();
		startEventTask();
	}

	private void startEventTask() {
		ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				if (!isAlreadyDead()) {
					count++;
					if (count < 7) {
						SkillEngine.getInstance().getSkill(getOwner(), 19658, 60, getOwner()).useNoAnimationSkill();
						startEventTask();
					}
					else {
						delete();
					}
				}

			}

		}, 4000);
	}

	private void delete() {
		AI2Actions.deleteOwner(this);
	}
}
