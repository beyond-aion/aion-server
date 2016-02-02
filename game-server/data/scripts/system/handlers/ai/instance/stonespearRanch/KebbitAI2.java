package ai.instance.stonespearRanch;

import java.util.concurrent.Future;

import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.ai2.poll.AIAnswer;
import com.aionemu.gameserver.ai2.poll.AIAnswers;
import com.aionemu.gameserver.ai2.poll.AIQuestion;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.utils.ThreadPoolManager;

import ai.GeneralNpcAI2;

/**
 * @author Yeats
 */
@AIName("stonespear_kebbit")
public class KebbitAI2 extends GeneralNpcAI2 {

	private Future<?> despawnTask;

	@Override
	public void handleSpawned() {
		super.handleSpawned();
		startDespawnTask();
	}

	private void startDespawnTask() {
		if (despawnTask != null) {
			return;
		}
		despawnTask = ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				if (getOwner() != null && !getOwner().getLifeStats().isAlreadyDead()) {
					getOwner().getController().onDelete();
				}
			}
		}, 15500); // 15,5s
	}

	@Override
	public void handleDied() {
		cancelTask();
		super.handleDied();
		getOwner().getController().onDelete();
	}

	private void cancelTask() {
		if (despawnTask != null && !despawnTask.isCancelled()) {
			despawnTask.cancel(false);
		}
	}
	
	@Override
	protected AIAnswer pollInstance(AIQuestion question) {
		switch (question) {
			case SHOULD_DECAY:
				return AIAnswers.POSITIVE;
			case SHOULD_RESPAWN:
				return AIAnswers.NEGATIVE;
			case SHOULD_REWARD:
				return AIAnswers.NEGATIVE;
			case SHOULD_LOOT:
				return AIAnswers.NEGATIVE;
			case CAN_ATTACK_PLAYER:
				return AIAnswers.NEGATIVE;
			default:
				return null;
		}
	}
	
	@Override
	protected void handleAttack(Creature creature) {
		//do nothing
	}
}
