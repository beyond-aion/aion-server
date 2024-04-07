package ai.instance.abyssal_splinter;

import java.util.concurrent.Future;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.HpPhases;
import com.aionemu.gameserver.ai.poll.AIQuestion;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.utils.ThreadPoolManager;

import ai.AggressiveNpcAI;

/**
 * @author Ritsu, Luzien
 */
@AIName("ebonsoul")
public class EbonsoulAI extends AggressiveNpcAI implements HpPhases.PhaseHandler {

	private final HpPhases hpPhases = new HpPhases(95);
	private Future<?> skillTask;

	public EbonsoulAI(Npc owner) {
		super(owner);
	}

	@Override
	protected void handleAttack(Creature creature) {
		super.handleAttack(creature);
		hpPhases.tryEnterNextPhase(this);
	}

	@Override
	public void handleHpPhase(int phaseHpPercent) {
		startSkillTask();
	}

	private void startSkillTask() {
		skillTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(() -> {
			if (isDead())
				cancelTask();
			else {
				SkillEngine.getInstance().getSkill(getOwner(), 19159, 55, getOwner()).useNoAnimationSkill();
				if (getPosition().getWorldMapInstance().getNpc(281908) == null) {
					spawn(281908, 462.47913f, 707.4807f, 433.78372f, (byte) 93);
					spawn(281908, 456.09427f, 707.4807f, 433.78372f, (byte) 93);
				}
			}
		}, 5000, 70000); // re-check delay
	}

	private void cancelTask() {
		if (skillTask != null && !skillTask.isCancelled()) {
			skillTask.cancel(true);
		}
	}

	@Override
	protected void handleDied() {
		super.handleDied();
		cancelTask();
	}

	@Override
	protected void handleBackHome() {
		super.handleBackHome();
		cancelTask();
		hpPhases.reset();
		getEffectController().removeEffect(19266);
	}

	@Override
	protected void handleDespawned() {
		cancelTask();
		super.handleDespawned();
	}

	@Override
	public boolean ask(AIQuestion question) {
		return switch (question) {
			case REWARD_LOOT, REWARD_AP -> false;
			default -> super.ask(question);
		};
	}

}
