package ai.instance.unstableSplinterpath;

import java.util.concurrent.Future;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.ai.AIActions;
import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.AIState;
import com.aionemu.gameserver.ai.manager.EmoteManager;
import com.aionemu.gameserver.ai.poll.AIQuestion;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.state.CreatureState;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.utils.ThreadPoolManager;

import ai.GeneralNpcAI;

/**
 * @author Ritsu, Cheatkiller
 */
@AIName("summonedunstablelapilima")
public class UnstableGatesSummonedAI extends GeneralNpcAI {

	private Future<?> eventTask;
	private boolean canThink = true;

	public UnstableGatesSummonedAI(Npc owner) {
		super(owner);
	}

	@Override
	protected void handleSpawned() {
		super.handleSpawned();
		startMove();
	}

	@Override
	public boolean canThink() {
		return canThink;
	}

	@Override
	protected void handleDied() {
		cancelEventTask();
		super.handleDied();
	}

	@Override
	protected void handleDespawned() {
		cancelEventTask();
		super.handleDespawned();
	}

	@Override
	protected void handleMoveArrived() {
		super.handleMoveArrived();
		startEventTask();
	}

	private void startMove() {
		canThink = false;
		EmoteManager.emoteStopAttacking(getOwner());
		setStateIfNot(AIState.FOLLOWING);
		getOwner().setState(CreatureState.ACTIVE, true);
		AIActions.targetCreature(this, getPosition().getWorldMapInstance().getNpc(219563));
		getMoveController().moveToTargetObject();
	}

	private void cancelEventTask() {
		if (eventTask != null && !eventTask.isDone())
			eventTask.cancel(true);
	}

	private void startEventTask() {
		eventTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(() -> {
			Npc boss = getPosition().getWorldMapInstance().getNpc(219563);
			if (boss == null || boss.isDead()) {
				cancelEventTask();
				return;
			}
			SkillEngine.getInstance().getSkill(getOwner(), Rnd.nextBoolean() ? 19257 : 19281, 55, boss).useNoAnimationSkill();
		}, 5000, 30000);
	}

	@Override
	public boolean ask(AIQuestion question) {
		return switch (question) {
			case REWARD_LOOT, REWARD_AP -> false;
			default -> super.ask(question);
		};
	}
}
