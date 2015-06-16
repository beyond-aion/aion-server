package ai.instance.aturamSkyFortress;

import java.util.concurrent.Future;

import ai.AggressiveNpcAI2;

import com.aionemu.commons.network.util.ThreadPoolManager;
import com.aionemu.gameserver.ai2.AI2Actions;
import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.ai2.poll.AIAnswer;
import com.aionemu.gameserver.ai2.poll.AIAnswers;
import com.aionemu.gameserver.ai2.poll.AIQuestion;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.utils.MathUtil;

/**
 *
 * @author xTz
 */
@AIName("shulack_guided_bomb")
public class ShulackGuidedBombAI2 extends AggressiveNpcAI2 {
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
				if (!isAlreadyDead() && !isDestroyed) {
					despawn();
				}
			}

		}, 10000);
	}

	private void doSchedule(final Creature creature) {

		task = ThreadPoolManager.getInstance().scheduleAtFixedRate(new Runnable() {

			@Override
			public void run() {
				if (!isAlreadyDead() && !isDestroyed) {
					destroy(creature);
				}
				else {
					if (task != null) {
						task.cancel(true);
					}	
				}
			}

		}, 1000, 1000);

	}

	private void despawn() {
		if (!isAlreadyDead()) {
			AI2Actions.deleteOwner(this);
		}
	}

	private void destroy(Creature creature) {
		if (!isDestroyed && !isAlreadyDead()) {
			if (creature != null && MathUtil.getDistance(getOwner(), creature) <= 4) {
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
	public AIAnswer ask(AIQuestion question) {
		switch (question) {
			case CAN_RESIST_ABNORMAL:
				return AIAnswers.POSITIVE;
			default:
				return AIAnswers.NEGATIVE;
		}
	}

	@Override
	protected AIAnswer pollInstance(AIQuestion question) {
		switch (question) {
			case SHOULD_DECAY:
				return AIAnswers.NEGATIVE;
			case SHOULD_RESPAWN:
				return AIAnswers.NEGATIVE;
			case SHOULD_REWARD:
				return AIAnswers.NEGATIVE;
			default:
				return null;
		}
	}

}