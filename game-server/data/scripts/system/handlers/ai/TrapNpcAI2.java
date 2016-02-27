package ai;

import java.util.concurrent.Future;

import com.aionemu.gameserver.ai2.AI2Actions;
import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.ai2.AIState;
import com.aionemu.gameserver.ai2.NpcAI2;
import com.aionemu.gameserver.ai2.poll.AIAnswer;
import com.aionemu.gameserver.ai2.poll.AIAnswers;
import com.aionemu.gameserver.ai2.poll.AIQuestion;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.state.CreatureVisualState;
import com.aionemu.gameserver.model.skill.NpcSkillEntry;
import com.aionemu.gameserver.network.aion.serverpackets.SM_PLAYER_STATE;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;

/**
 * @author ATracer
 */
@AIName("trap")
public class TrapNpcAI2 extends NpcAI2 {

	private Future<?> despawnTask;

	@Override
	protected void handleCreatureSee(Creature creature) {
		super.handleCreatureSee(creature);
		tryActivateTrap(creature);
	}

	@Override
	protected void handleCreatureMoved(Creature creature) {
		super.handleCreatureMoved(creature);
		tryActivateTrap(creature);
	}

	private void tryActivateTrap(Creature creature) {
		if (despawnTask != null) {
			return;
		}

		if (!creature.getLifeStats().isAlreadyDead() && !creature.isInVisualState(CreatureVisualState.BLINKING)
			&& isInRange(creature, getOwner().getGameStats().getAttackRange().getCurrent())) {

			Creature creator = (Creature) getCreator();
			if (!creator.isEnemy(creature)) {
				return;
			}
			explode(creature);
		}
	}

	@Override
	protected void handleSpawned() {
		super.handleSpawned();
		if (getObjectTemplate().getName().toLowerCase().equals("shock trap")) {
			explode(getOwner());
		}
	}

	private void explode(final Creature creature) {
		if (setStateIfNot(AIState.FIGHT)) {
			getOwner().unsetVisualState(CreatureVisualState.HIDE1);
			PacketSendUtility.broadcastPacket(getOwner(), new SM_PLAYER_STATE(getOwner()));
			AI2Actions.targetCreature(TrapNpcAI2.this, creature);
			getOwner().getKnownList().doUpdate();
			NpcSkillEntry npcSkill = getSkillList().getRandomSkill();
			if (npcSkill != null) {
				AI2Actions.useSkill(TrapNpcAI2.this, npcSkill.getSkillId());
			}
			despawnTask = ThreadPoolManager.getInstance().schedule(new TrapDelete(TrapNpcAI2.this), 5000);
		}
	}

	@Override
	public boolean isMoveSupported() {
		return false;
	}

	@Override
	protected AIAnswer pollInstance(AIQuestion question) {
		switch (question) {
			case SHOULD_DECAY:
				return AIAnswers.NEGATIVE;
			case SHOULD_RESPAWN:
				return AIAnswers.NEGATIVE;
			case SHOULD_REWARD:
				return AIAnswers.NEGATIVE;
			default:
				return null;
		}
	}

	private static final class TrapDelete implements Runnable {

		private TrapNpcAI2 ai;

		TrapDelete(TrapNpcAI2 ai) {
			this.ai = ai;
		}

		@Override
		public void run() {
			AI2Actions.deleteOwner(ai);
			ai = null;
		}

	}

}
