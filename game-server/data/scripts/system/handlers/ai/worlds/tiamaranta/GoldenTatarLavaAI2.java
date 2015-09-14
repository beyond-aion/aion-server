package ai.worlds.tiamaranta;

import java.util.concurrent.Future;

import ai.AggressiveNpcAI2;

import com.aionemu.commons.network.util.ThreadPoolManager;
import com.aionemu.gameserver.ai2.AI2Actions;
import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.ai2.poll.AIAnswer;
import com.aionemu.gameserver.ai2.poll.AIAnswers;
import com.aionemu.gameserver.ai2.poll.AIQuestion;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.world.WorldPosition;

/**
 * @author xTz
 */
@AIName("golden_tatar_lava")
public class GoldenTatarLavaAI2 extends AggressiveNpcAI2 {

	private Future<?> task;
	private int spawnCount;

	@Override
	public boolean canThink() {
		return false;
	}

	@Override
	protected void handleSpawned() {
		super.handleSpawned();
		if (getNpcId() == 282746) {
			startSpawnTask();
		} else {
			ThreadPoolManager.getInstance().schedule(new Runnable() {

				@Override
				public void run() {
					if (!isAlreadyDead()) {
						SkillEngine.getInstance().getSkill(getOwner(), 20215, 60, getOwner()).useNoAnimationSkill();
						AI2Actions.deleteOwner(GoldenTatarLavaAI2.this);
					}
				}

			}, 500);

		}
	}

	private void startSpawnTask() {
		task = ThreadPoolManager.getInstance().scheduleAtFixedRate(new Runnable() {

			@Override
			public void run() {
				if (isAlreadyDead()) {
					cancelTask();
				} else {
					spawnCount++;
					WorldPosition p = getPosition();
					spawn(282747, p.getX(), p.getY(), p.getZ(), p.getHeading());
					if (spawnCount >= 20) {
						cancelTask();
						AI2Actions.deleteOwner(GoldenTatarLavaAI2.this);
					}
				}
			}

		}, 3000, 3000);
	}

	private void cancelTask() {
		if (task != null && !task.isDone()) {
			task.cancel(true);
		}
	}

	@Override
	public AIAnswer ask(AIQuestion question) {
		switch (question) {
			case CAN_ATTACK_PLAYER:
				return AIAnswers.POSITIVE;
			default:
				return AIAnswers.NEGATIVE;
		}
	}

	@Override
	protected void handleDespawned() {
		cancelTask();
		super.handleDespawned();
	}

	@Override
	protected void handleDied() {
		cancelTask();
		super.handleDied();
		AI2Actions.deleteOwner(this);
	}

}
