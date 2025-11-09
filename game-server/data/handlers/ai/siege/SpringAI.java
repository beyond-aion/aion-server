package ai.siege;

import java.util.concurrent.Future;

import com.aionemu.gameserver.ai.AIActions;
import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.NpcAI;
import com.aionemu.gameserver.ai.poll.AIQuestion;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.utils.ThreadPoolManager;

/**
 * @author xTz, Sykra
 */
@AIName("spring")
public class SpringAI extends NpcAI {

	private Future<?> healCheckTask;

	public SpringAI(Npc owner) {
		super(owner);
	}

	@Override
	public void handleSpawned() {
		super.handleSpawned();
		healCheckTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(this::checkForHeal, 5000, 5000);
	}

	@Override
	protected void handleDied() {
		super.handleDied();
		cancelTasks();
	}

	@Override
	protected void handleDespawned() {
		super.handleDespawned();
		cancelTasks();
	}

	@Override
	public boolean ask(AIQuestion question) {
		return switch (question) {
			case ALLOW_DECAY, REWARD_AP_XP_DP_LOOT, REWARD_LOOT -> false;
			default -> super.ask(question);
		};
	}

	private void cancelTasks() {
		if (healCheckTask != null && !healCheckTask.isDone()) {
			healCheckTask.cancel(true);
			healCheckTask = null;
		}
	}

	private void checkForHeal() {
		if (isDead() || !getPosition().isSpawned())
			return;
		boolean targetNearby = getKnownList().stream()
			.anyMatch(o -> o.get() instanceof Creature creature && getRace() == creature.getRace() && !creature.isDead() && !creature.getLifeStats().isFullyRestoredHp()
			&& isInRange(creature, 10) && !creature.getEffectController().hasAbnormalEffect(19116));
		if (targetNearby)
			doHeal();
	}

	private void doHeal() {
		AIActions.targetSelf(this);
		AIActions.useSkill(this, 19116);
	}

}
