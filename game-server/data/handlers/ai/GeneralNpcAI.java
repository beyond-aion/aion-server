package ai;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.AIState;
import com.aionemu.gameserver.ai.AttackIntention;
import com.aionemu.gameserver.ai.NpcAI;
import com.aionemu.gameserver.ai.event.AIEventType;
import com.aionemu.gameserver.ai.handler.*;
import com.aionemu.gameserver.ai.manager.SkillAttackManager;
import com.aionemu.gameserver.controllers.attack.AggroTarget;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.skill.NpcSkillEntry;

/**
 * @author ATracer
 */
@AIName("general")
public class GeneralNpcAI extends NpcAI {

	public GeneralNpcAI(Npc owner) {
		super(owner);
	}

	@Override
	public void think() {
		ThinkEventHandler.onThink(this);
	}

	@Override
	protected void handleAttack(Creature creature) {
		AttackEventHandler.onAttack(this, creature);
	}

	@Override
	protected boolean handleCreatureNeedsSupport(Creature creature) {
		return AggroEventHandler.onCreatureNeedsSupport(this, creature);
	}

	@Override
	protected void handleCreatureNotSee(Creature creature) {
		if (creature.equals(getTarget())) {
			getOwner().getController().abortCast();
			onGeneralEvent(AIEventType.TARGET_TOOFAR);
		}
	}

	@Override
	protected void handleDialogStart(Player player) {
		TalkEventHandler.onTalk(this, player);
	}

	@Override
	protected void handleDialogFinish(Player creature) {
		TalkEventHandler.onFinishTalk(this, creature);
	}

	@Override
	protected void handleFinishAttack() {
		AttackEventHandler.onFinishAttack(this);
	}

	@Override
	protected void handleAttackComplete() {
		AttackEventHandler.onAttackComplete(this);
	}

	@Override
	protected void handleNotAtHome() {
		ReturningEventHandler.onNotAtHome(this);
	}

	@Override
	protected void handleBackHome() {
		ReturningEventHandler.onBackHome(this);
	}

	@Override
	protected void handleTargetTooFar() {
		TargetEventHandler.onTargetTooFar(this);
	}

	@Override
	protected void handleTargetGiveup() {
		TargetEventHandler.onTargetGiveup(this);
	}

	@Override
	protected void handleTargetChanged(Creature creature) {
		super.handleTargetChanged(creature);
		TargetEventHandler.onTargetChange(this, creature);
	}

	@Override
	protected void handleMoveArrived() {
		super.handleMoveArrived();
		MoveEventHandler.onMoveArrived(this);
	}

	@Override
	public void handleCreatureDetected(Creature creature) {
		getOwner().getPosition().getWorldMapInstance().getInstanceHandler().onCreatureDetected(getOwner(), creature);
	}

	@Override
	protected boolean canHandleEvent(AIEventType eventType) {
		switch (eventType) {
			case CREATURE_NEEDS_SUPPORT:
				return getState() == AIState.IDLE || getState() == AIState.WALKING;
		}
		return super.canHandleEvent(eventType);
	}

	@Override
	public AttackIntention chooseAttackIntention() {
		if (!(getTarget() instanceof Creature target) || !getAggroList().isHating(target)) {
			Creature mostHated = getAggroList().getTarget(AggroTarget.MOST_HATED);
			if (mostHated == null)
				return AttackIntention.FINISH_ATTACK;
			onCreatureEvent(AIEventType.TARGET_CHANGED, mostHated);
		}

		if (chooseSkillAttack(getOwner().getObjectTemplate().getAttackRange() == 0))
			return AttackIntention.SKILL_ATTACK;

		return AttackIntention.SIMPLE_ATTACK;
	}

	protected final boolean chooseSkillAttack(boolean alwaysRandomSkill) {
		NpcSkillEntry skill = alwaysRandomSkill ? getOwner().getSkillList().getRandomSkill() : SkillAttackManager.chooseNextSkill(this);
		if (skill != null) {
			getOwner().getGameStats().setLastSkill(skill);
			getOwner().removeNextQueuedSkill(skill);
			return true;
		}
		return false;
	}
}
