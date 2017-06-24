package ai.instance.rentusBase;

import java.util.concurrent.Future;

import com.aionemu.gameserver.ai.AIActions;
import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.poll.AIQuestion;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.world.WorldPosition;

import ai.GeneralNpcAI;

/**
 * @author xTz
 */
@AIName("dancing_flame")
public class DancingFlameAI extends GeneralNpcAI {

	private Future<?> task;

	private void startTask() {
		task = ThreadPoolManager.getInstance().scheduleAtFixedRate(new Runnable() {

			@Override
			public void run() {
				if (isDead()) {
					cancelTask();
				} else {
					if (isPlayerInRange()) {
						WorldPosition p = getPosition();
						if (getNpcId() == 282996) {
							spawn(282998, p.getX(), p.getY(), p.getZ(), p.getHeading());
						} else {
							spawn(282999, p.getX(), p.getY(), p.getZ(), p.getHeading());
						}
					}
				}
			}

		}, 3000, 3000);
	}

	private boolean isPlayerInRange() {
		for (Player player : getKnownList().getKnownPlayers().values()) {
			if (isInRange(player, 30)) {
				return true;
			}
		}
		return false;
	}

	private void cancelTask() {
		if (task != null && !task.isDone()) {
			task.cancel(true);
		}
	}

	@Override
	protected void handleSpawned() {
		super.handleSpawned();
		if (getNpcId() == 282996 || getNpcId() == 282997) {
			startTask();
		} else {
			ThreadPoolManager.getInstance().schedule(new Runnable() {

				@Override
				public void run() {
					SkillEngine.getInstance().getSkill(getOwner(), getNpcId() == 282998 ? 20536 : 20535, 60, getOwner()).useNoAnimationSkill();
				}

			}, 500);
			starLifeTask();
		}
	}

	private void starLifeTask() {
		ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				despawn();
			}

		}, 4000);
	}

	private void despawn() {
		if (!isDead()) {
			AIActions.deleteOwner(this);
		}
	}

	@Override
	protected void handleDespawned() {
		super.handleDespawned();
		cancelTask();
	}

	@Override
	protected void handleDied() {
		super.handleDied();
		cancelTask();
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
