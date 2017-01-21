package ai.instance.empyreanCrucible;

import com.aionemu.gameserver.ai.AIActions;
import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.poll.AIQuestion;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.utils.ThreadPoolManager;

import ai.GeneralNpcAI;

/**
 * @author Luzien
 */
@AIName("spark_of_darkness")
public class SparkOfDarknessAI extends GeneralNpcAI {

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
				AIActions.deleteOwner(SparkOfDarknessAI.this);
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
