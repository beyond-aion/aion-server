package ai.instance.aturamSkyFortress;

import java.util.concurrent.Future;

import com.aionemu.gameserver.ai.AIActions;
import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.poll.AIQuestion;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
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
	private Future<?> task;

	public ShulackGuidedBombAI(Npc owner) {
		super(owner);
	}

	@Override
	protected void handleDespawned() {
		super.handleDespawned();
		if (task != null)
			task.cancel(true);
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
		ThreadPoolManager.getInstance().schedule(() -> {
			if (!isDead() && !isDestroyed)
				despawn();
		}, 10000);
	}

	private void doSchedule(final Creature creature) {

		task = ThreadPoolManager.getInstance().scheduleAtFixedRate(() -> {
			if (!isDead() && !isDestroyed) {
				destroy(creature);
			} else {
				if (task != null) {
					task.cancel(true);
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
				ThreadPoolManager.getInstance().schedule(this::despawn, 3200);
			}
		}
	}

	@Override
	public boolean ask(AIQuestion question) {
		return switch (question) {
			case ALLOW_DECAY, ALLOW_RESPAWN, REWARD_AP_XP_DP_LOOT -> false;
			case IS_IMMUNE_TO_ABNORMAL_STATES -> true;
			default -> super.ask(question);
		};
	}
}
