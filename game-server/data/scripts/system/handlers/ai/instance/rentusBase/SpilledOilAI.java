package ai.instance.rentusBase;

import com.aionemu.gameserver.ai.AIActions;
import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.utils.ThreadPoolManager;

import ai.GeneralNpcAI;

/**
 * @author xTz
 */
@AIName("spilled_oil")
public class SpilledOilAI extends GeneralNpcAI {

	private int count;

	public SpilledOilAI(Npc owner) {
		super(owner);
	}

	@Override
	protected void handleSpawned() {
		super.handleSpawned();
		startEventTask();
	}

	private void startEventTask() {
		ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				if (!isDead()) {
					count++;
					if (count < 7) {
						SkillEngine.getInstance().getSkill(getOwner(), 19658, 60, getOwner()).useNoAnimationSkill();
						startEventTask();
					} else {
						delete();
					}
				}

			}

		}, 4000);
	}

	private void delete() {
		AIActions.deleteOwner(this);
	}
}
