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
@AIName("rukril")
public class RukrilAI extends AggressiveNpcAI implements HpPhases.PhaseHandler {

	private final HpPhases hpPhases = new HpPhases(95);
	private Future<?> skillTask;

	public RukrilAI(Npc owner) {
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
				SkillEngine.getInstance().getSkill(getOwner(), 19266, 55, getOwner()).useNoAnimationSkill();
				if (getPosition().getWorldMapInstance().getNpc(281907) == null) {
					spawn(281907, 447.3828f, 675.9968f, 433.95636f, (byte) 19);
					spawn(281907, 441.49512f, 680.38495f, 434.02753f, (byte) 19);
				}
			}
		}, 5000, 70000);
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
