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
 * @author Ritsu, Luzien, Cheatkiler
 */
@AIName("unstablerukril")
public class UnstableRukrilAI extends AggressiveNpcAI implements HpPhases.PhaseHandler {

	private final HpPhases hpPhases = new HpPhases(95);
	private Future<?> skillTask;

	public UnstableRukrilAI(Npc owner) {
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
		final Npc ebonsoul = getPosition().getWorldMapInstance().getNpc(219552);
		skillTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(() -> {
			if (isDead())
				cancelTask();
			else {
				if (getPosition().getWorldMapInstance().getNpc(283204) == null) {
					SkillEngine.getInstance().getSkill(getOwner(), 19266, 55, getOwner()).useNoAnimationSkill();
					spawn(283204, getOwner().getX() + 2, getOwner().getY() - 2, getOwner().getZ(), (byte) 0);
				}
				if (ebonsoul != null && !ebonsoul.isDead()) {
					SkillEngine.getInstance().getSkill(ebonsoul, 19159, 55, ebonsoul).useNoAnimationSkill();
					spawn(283205, ebonsoul.getX() + 2, ebonsoul.getY() - 2, ebonsoul.getZ(), (byte) 0);
				}
			}
		}, 5000, 70000);
	}

	private void cancelTask() {
		if (skillTask != null && !skillTask.isCancelled()) {
			skillTask.cancel(true);
		}
	}

	private void tryRegen() {
		Npc ebonsoul = getPosition().getWorldMapInstance().getNpc(219552);
		if (ebonsoul != null && !ebonsoul.isDead() && PositionUtil.isInRange(getOwner(), ebonsoul, 5))
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
