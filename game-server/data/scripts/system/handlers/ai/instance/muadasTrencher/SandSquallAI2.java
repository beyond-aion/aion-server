package ai.instance.muadasTrencher;

import java.util.concurrent.Future;

import ai.AggressiveNpcAI2;

import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.ai2.AI2Actions;
import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.ai2.poll.AIQuestion;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.world.WorldMapInstance;
import com.aionemu.gameserver.world.WorldPosition;

/**
 * @author xTz
 */
@AIName("sand_squall")
public class SandSquallAI2 extends AggressiveNpcAI2 {

	private Future<?> lifeTask;

	@Override
	public boolean canThink() {
		return false;
	}

	@Override
	protected void handleSpawned() {
		super.handleSpawned();
		startLifeTask();
		castSkillTask(19896, 500);
		castSkillTask(19894, 500);
		castSkillTask(19894, 2500);
		castSkillTask(20444, 4500);
		castSkillTask(19894, 6500);
		castSkillTask(19894, 8500);
		castSkillTask(19894, 10500);
		castSkillTask(20444, 12500);
		castSkillTask(19894, 14500);
		castSkillTask(19894, 16500);
		castSkillTask(19895, 18500);
	}

	@Override
	protected void handleDespawned() {
		cancelLifeTask();
		super.handleDespawned();
	}

	@Override
	protected void handleDied() {
		cancelLifeTask();
		super.handleDied();
		AI2Actions.deleteOwner(this);
	}

	private void castSkillTask(final int skill, int time) {
		ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				if (!isAlreadyDead()) {
					WorldPosition p = getPosition();
					if (p != null) {
						WorldMapInstance instance = p.getWorldMapInstance();
						if (instance != null) {
							SkillEngine.getInstance().getSkill(getOwner(), skill, 60, getOwner()).useNoAnimationSkill();
						}
					}
				}
			}

		}, time);
	}

	private void startLifeTask() {
		lifeTask = ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				if (!isAlreadyDead()) {
					AI2Actions.deleteOwner(SandSquallAI2.this);
				}
			}

		}, 20000);
	}

	private void cancelLifeTask() {
		if (lifeTask != null && !lifeTask.isDone())
			lifeTask.cancel(true);
	}

	@Override
	public boolean ask(AIQuestion question) {
		switch (question) {
			case CAN_ATTACK_PLAYER:
			case CAN_RESIST_ABNORMAL:
				return true;
			case SHOULD_DECAY:
			case SHOULD_RESPAWN:
			case SHOULD_REWARD:
				return false;
			default:
				return super.ask(question);
		}
	}
}
