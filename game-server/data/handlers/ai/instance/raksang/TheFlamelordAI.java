package ai.instance.raksang;

import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.HpPhases;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;

import ai.AggressiveNpcAI;

/**
 * @author xTz
 */
@AIName("the_flamelord")
public class TheFlamelordAI extends AggressiveNpcAI implements HpPhases.PhaseHandler {

	private final HpPhases hpPhases = new HpPhases(90, 40, 30, 20, 10);
	private AtomicBoolean isAggred = new AtomicBoolean(false);
	private Future<?> phaseTask;

	public TheFlamelordAI(Npc owner) {
		super(owner);
	}

	@Override
	public void handleHpPhase(int phaseHpPercent) {
		switch (phaseHpPercent) {
			case 90 -> startPhaseTask();
			case 40, 30, 20, 10 -> startPhaseEvent(phaseHpPercent);
		}
	}

	private void startPhaseEvent(final int percent) {
		cancelPhaseTask();
		PacketSendUtility.broadcastMessage(getOwner(), 1401120);
		SkillEngine.getInstance().getSkill(getOwner(), 19980, 46, getOwner()).useNoAnimationSkill();
		ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				if (!isDead()) {
					switch (percent) {
						case 40:
							moveExecutor(282451);
							break;
						case 30:
							moveExecutor(282451);
							moveExecutor(282452);
							break;
						case 20:
							moveExecutor(282451);
							moveExecutor(282452);
							moveExecutor(282453);
							break;
						case 10:
							moveExecutor(282451);
							moveExecutor(282452);
							moveExecutor(282453);
							moveExecutor(282454);
							break;
					}
					SkillEngine.getInstance().getSkill(getOwner(), 19924, 44, getOwner()).useNoAnimationSkill();
					cancelPhaseTask();
					startPhaseTask();
				}
			}

		}, 5000);
	}

	private void moveExecutor(final int executorId) {
		final Npc npc = (Npc) spawn(executorId, 802.845f, 964.903f, 792.102f, (byte) 0);
		ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				if (!isDead()) {
					int targetId = 0;
					switch (executorId) {
						case 282451:
							targetId = 701062;
							break;
						case 282452:
							targetId = 701063;
							break;
						case 282453:
							targetId = 701064;
							break;
						case 282454:
							targetId = 701065;
							break;
					}
					Npc target = getPosition().getWorldMapInstance().getNpc(targetId);
					if (target != null) {
						npc.setTarget(target);
						npc.getMoveController().moveToTargetObject();
					}
				}
			}

		}, 1500);
	}

	private void startPhaseTask() {
		phaseTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(new Runnable() {

			@Override
			public void run() {
				if (isDead()) {
					cancelPhaseTask();
				} else {
					SkillEngine.getInstance().getSkill(getOwner(), 19925, 44, getOwner()).useNoAnimationSkill();
					PacketSendUtility.broadcastMessage(getOwner(), 1401119);
				}
			}

		}, 3000, 30000);
	}

	private void cancelPhaseTask() {
		if (phaseTask != null && !phaseTask.isDone()) {
			phaseTask.cancel(true);
		}
	}

	@Override
	protected void handleDied() {
		cancelPhaseTask();
		getPosition().getWorldMapInstance().setDoorState(118, true);
		PacketSendUtility.broadcastMessage(getOwner(), 1401121);
		super.handleDied();
	}

	@Override
	protected void handleDespawned() {
		cancelPhaseTask();
		super.handleDespawned();
	}

	@Override
	protected void handleAttack(Creature creature) {
		if (isAggred.compareAndSet(false, true)) {
			PacketSendUtility.broadcastMessage(getOwner(), 1401118);
		}
		super.handleAttack(creature);
		hpPhases.tryEnterNextPhase(this);
	}

	@Override
	protected void handleBackHome() {
		cancelPhaseTask();
		isAggred.set(false);
		super.handleBackHome();
		hpPhases.reset();
	}

}
