package ai.instance.argentManor;

import java.util.concurrent.atomic.AtomicBoolean;

import com.aionemu.gameserver.ai2.AI2Actions;
import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.ai2.AIState;
import com.aionemu.gameserver.ai2.poll.AIQuestion;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;

import ai.GeneralNpcAI2;

/**
 * @author xTz
 */
@AIName("cadellas_hetgolem")
public class CadellasHetgolemAI2 extends GeneralNpcAI2 {

	private AtomicBoolean isDestroyed = new AtomicBoolean(false);

	@Override
	public boolean canThink() {
		return false;
	}

	@Override
	protected void handleSpawned() {
		super.handleSpawned();
		setStateIfNot(AIState.FOLLOWING);
		ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				if (!isAlreadyDead()) {
					SkillEngine.getInstance().getSkill(getOwner(), 19571, 55, getOwner()).useNoAnimationSkill();
				}
			}

		}, 1000);
	}

	@Override
	protected void handleMoveArrived() {
		super.handleMoveArrived();
		getMoveController().abortMove();
		if (isDestroyed.compareAndSet(false, true)) {
			PacketSendUtility.broadcastMessage(getOwner(), 1500462);
			SkillEngine.getInstance().getSkill(getOwner(), getHealSkill(), 60, getOwner()).useNoAnimationSkill();
			startDespawnTask();
		}
	}

	private void startDespawnTask() {
		ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				if (!isAlreadyDead()) {
					AI2Actions.deleteOwner(CadellasHetgolemAI2.this);
				}
			}

		}, 4000);
	}

	private int getHealSkill() {
		switch (getNpcId()) {
			case 282345:
				return 19525;
			case 282346:
				return 19526;
			case 282347:
				return 19527;
			case 282348:
				return 19528;
			case 282349:
				return 19529;
			default:
				return 0;
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
