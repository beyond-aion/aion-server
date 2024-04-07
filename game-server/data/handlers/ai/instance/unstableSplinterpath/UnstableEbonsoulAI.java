package ai.instance.unstableSplinterpath;

import java.util.concurrent.Future;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.HpPhases;
import com.aionemu.gameserver.ai.poll.AIQuestion;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ATTACK_STATUS.TYPE;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.utils.PositionUtil;
import com.aionemu.gameserver.utils.ThreadPoolManager;

import ai.AggressiveNpcAI;

/**
 * @author Ritsu, Luzien, Cheatkiller
 */
@AIName("unstableebonsoul")
public class UnstableEbonsoulAI extends AggressiveNpcAI implements HpPhases.PhaseHandler {

	private final HpPhases hpPhases = new HpPhases(95);
	private Future<?> skillTask;

	public UnstableEbonsoulAI(Npc owner) {
		super(owner);
	}

	@Override
	protected void handleAttack(Creature creature) {
		super.handleAttack(creature);
		hpPhases.tryEnterNextPhase(this);
		tryRegen();
	}

	@Override
	public void handleHpPhase(int phaseHpPercent) {
		startSkillTask();
	}

	private void startSkillTask() {
		final Npc rukril = getPosition().getWorldMapInstance().getNpc(219551);
		skillTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(() -> {
			if (isDead())
				cancelTask();
			else {
				if (getPosition().getWorldMapInstance().getNpc(283205) == null) {
					SkillEngine.getInstance().getSkill(getOwner(), 19159, 55, getOwner()).useNoAnimationSkill();
					spawn(283205, getOwner().getX() + 2, getOwner().getY() - 2, getOwner().getZ(), (byte) 0);
				}
				if (rukril != null && !rukril.isDead()) {
					SkillEngine.getInstance().getSkill(rukril, 19266, 55, rukril).useNoAnimationSkill();
					spawn(283204, rukril.getX() + 2, rukril.getY() - 2, rukril.getZ(), (byte) 0);
				}
			}
		}, 5000, 70000); // re-check delay
	}

	private void cancelTask() {
		if (skillTask != null && !skillTask.isCancelled()) {
			skillTask.cancel(true);
		}
	}

	private void tryRegen() {
		Npc rukril = getPosition().getWorldMapInstance().getNpc(219551);
		if (rukril != null && !rukril.isDead() && PositionUtil.isInRange(getOwner(), rukril, 5))
			if (!getOwner().getLifeStats().isFullyRestoredHp())
				getOwner().getLifeStats().increaseHp(TYPE.HP, 10000);

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
