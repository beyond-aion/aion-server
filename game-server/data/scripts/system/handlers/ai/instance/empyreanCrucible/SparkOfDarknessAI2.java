package ai.instance.empyreanCrucible;

import com.aionemu.gameserver.ai2.AI2Actions;
import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.ai2.poll.AIQuestion;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.utils.ThreadPoolManager;

import ai.GeneralNpcAI2;

/**
 * @author Luzien
 */
@AIName("spark_of_darkness")
public class SparkOfDarknessAI2 extends GeneralNpcAI2 {

	@Override
	protected void handleSpawned() {
		super.handleSpawned();
		startEventTask();
		startLifeTask();
	}

	private void startEventTask() {
		ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				if (!isAlreadyDead()) {
					SkillEngine.getInstance().getSkill(getOwner(), 19554, 1, getOwner()).useNoAnimationSkill();
				}
			}

		}, 500);
	}

	private void startLifeTask() {
		ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				AI2Actions.deleteOwner(SparkOfDarknessAI2.this);
			}

		}, 6500);
	}

	@Override
	public boolean ask(AIQuestion question) {
		switch (question) {
			case CAN_ATTACK_PLAYER:
				return true;
			default:
				return super.ask(question);
		}
	}
}
