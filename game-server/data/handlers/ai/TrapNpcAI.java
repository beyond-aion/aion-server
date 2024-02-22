package ai;

import java.util.concurrent.Future;

import com.aionemu.gameserver.ai.AIActions;
import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.AIState;
import com.aionemu.gameserver.ai.NpcAI;
import com.aionemu.gameserver.ai.event.AIEventType;
import com.aionemu.gameserver.ai.poll.AIQuestion;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.state.CreatureVisualState;
import com.aionemu.gameserver.model.skill.NpcSkillEntry;
import com.aionemu.gameserver.network.aion.serverpackets.SM_PLAYER_STATE;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;

/**
 * @author ATracer
 */
@AIName("trap")
public class TrapNpcAI extends NpcAI {

	private Future<?> despawnTask;

	public TrapNpcAI(Npc owner) {
		super(owner);
	}

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

		if (!creature.isDead() && !creature.isInVisualState(CreatureVisualState.BLINKING)
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
		String name = getObjectTemplate().getName().toLowerCase();
		if (!name.equals("scrapped mechanisms"))
			getOwner().setVisualState(CreatureVisualState.HIDE1);
		super.handleSpawned();
		if (name.equals("shock trap"))
			explode(getOwner());

	}

	private void explode(final Creature creature) {
		if (setStateIfNot(AIState.FIGHT)) {
			getOwner().unsetVisualState(CreatureVisualState.HIDE1);
			PacketSendUtility.broadcastPacket(getOwner(), new SM_PLAYER_STATE(getOwner()));
			AIActions.targetCreature(TrapNpcAI.this, creature);
			getOwner().updateKnownlist();
			NpcSkillEntry npcSkill = getSkillList().getRandomSkill();
			if (npcSkill != null) {
				AIActions.useSkill(TrapNpcAI.this, npcSkill.getSkillId());
			}
			despawnTask = ThreadPoolManager.getInstance().schedule(() -> AIActions.deleteOwner(TrapNpcAI.this), 5000);
		}
	}

	@Override
	public boolean isMoveSupported() {
		return false;
	}

	@Override
	protected boolean canHandleEvent(AIEventType eventType) {
		if (eventType == AIEventType.CREATURE_MOVED)
			return true;
		return super.canHandleEvent(eventType);
	}

	@Override
	public boolean ask(AIQuestion question) {
		return switch (question) {
			case ALLOW_DECAY, ALLOW_RESPAWN, REWARD_AP_XP_DP_LOOT -> false;
			default -> super.ask(question);
		};
	}
}
