package ai.instance.aturamSkyFortress;

import java.util.concurrent.Future;

import com.aionemu.gameserver.ai.AIActions;
import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.poll.AIQuestion;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.utils.PositionUtil;
import com.aionemu.gameserver.utils.ThreadPoolManager;

import ai.AggressiveNpcAI;

/**
 * @author xTz
 */
@AIName("shulack_guided_bomb")
public class ShulackGuidedBombAI extends AggressiveNpcAI {

	private boolean isDestroyed;
	private boolean isHome = true;
	Future<?> task;

	@Override
	protected void handleDespawned() {
		super.handleDespawned();
		if (task != null) {
			task.cancel(true);
		}
	}

	@Override
	protected void handleSpawned() {
		super.handleSpawned();
		starLifeTask();
	}

	@Override
	protected void handleCreatureAggro(Creature creature) {
		super.handleCreatureAggro(creature);
		if (isHome) {
			isHome = false;
			doSchedule(creature);
		}
	}

	private void starLifeTask() {
		ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				if (!isDead() && !isDestroyed) {
					despawn();
				}
			}

		}, 10000);
	}

	private void doSchedule(final Creature creature) {

		task = ThreadPoolManager.getInstance().scheduleAtFixedRate(new Runnable() {

			@Override
			public void run() {
				if (!isDead() && !isDestroyed) {
					destroy(creature);
				} else {
					if (task != null) {
						task.cancel(true);
					}
				}
			}

		}, 1000, 1000);

	}

	private void despawn() {
		if (!isDead()) {
			AIActions.deleteOwner(this);
		}
	}

	private void destroy(Creature creature) {
		if (!isDestroyed && !isDead()) {
			if (creature != null && PositionUtil.getDistance(getOwner(), creature) <= 4) {
				isDestroyed = true;
				SkillEngine.getInstance().getSkill(getOwner(), 19415, 49, getOwner()).useNoAnimationSkill();
				ThreadPoolManager.getInstance().schedule(new Runnable() {

					@Override
					public void run() {
						despawn();
					}

				}, 3200);
			}
		}
	}

	@Override
	public boolean ask(AIQuestion question) {
		switch (question) {
			case SHOULD_DECAY:
			case SHOULD_RESPAWN:
			case SHOULD_REWARD:
				return false;
			case CAN_RESIST_ABNORMAL:
				return true;
			default:
				return super.ask(question);
		}
	}
}
