package ai;

import com.aionemu.gameserver.ai2.AI2Actions;
import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.ai2.AttackIntention;
import com.aionemu.gameserver.ai2.NpcAI2;
import com.aionemu.gameserver.ai2.event.AIEventType;
import com.aionemu.gameserver.ai2.handler.AggroEventHandler;
import com.aionemu.gameserver.ai2.handler.AttackEventHandler;
import com.aionemu.gameserver.ai2.handler.CreatureEventHandler;
import com.aionemu.gameserver.ai2.handler.DiedEventHandler;
import com.aionemu.gameserver.ai2.handler.MoveEventHandler;
import com.aionemu.gameserver.ai2.handler.ReturningEventHandler;
import com.aionemu.gameserver.ai2.handler.TalkEventHandler;
import com.aionemu.gameserver.ai2.handler.TargetEventHandler;
import com.aionemu.gameserver.ai2.handler.ThinkEventHandler;
import com.aionemu.gameserver.ai2.manager.SkillAttackManager;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.skill.NpcSkillEntry;
import com.aionemu.gameserver.model.templates.npcshout.ShoutEventType;
import com.aionemu.gameserver.skillengine.SkillEngine;

/**
 * @author ATracer
 */
@AIName("general")
public class GeneralNpcAI2 extends NpcAI2 {

	@Override
	public void think() {
		ThinkEventHandler.onThink(this);
	}

	@Override
	protected void handleDied() {
		DiedEventHandler.onDie(this);
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
	protected void handleTargetReached() {
		TargetEventHandler.onTargetReached(this);
	}

	@Override
	protected void handleNotAtHome() {
		ReturningEventHandler.onNotAtHome(this);
	}

	@Override
	protected void handleBackHome() {
		ReturningEventHandler.onBackHome(this);
		if (getSkillList().getUseInSpawnedSkill() != null) {
			int skillId = getSkillList().getUseInSpawnedSkill().getSkillId();
			int skillLevel = getSkillList().getSkillLevel(skillId);
			AI2Actions.targetSelf(this);
			SkillEngine.getInstance().getSkill(getOwner(), skillId, skillLevel, getOwner()).useNoAnimationSkill();
		}
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
	protected void handleMoveValidate() {
		MoveEventHandler.onMoveValidate(this);
	}

	@Override
	protected void handleMoveArrived() {
		super.handleMoveArrived();
		MoveEventHandler.onMoveArrived(this);
	}

	@Override
	protected void handleCreatureMoved(Creature creature) {
		CreatureEventHandler.onCreatureMoved(this, creature);
	}

	@Override
	protected void handleDespawned() {
		super.handleDespawned();
	}
	
	@Override
	public void handleCreatureDetected(Creature creature) {
		if (getOwner().getPosition().isInstanceMap())
			getOwner().getPosition().getWorldMapInstance().getInstanceHandler().onCreatureDetected(getOwner(), creature);
	}

	@Override
	protected boolean canHandleEvent(AIEventType eventType) {
		boolean canHandle = super.canHandleEvent(eventType);

		switch (eventType) {
			case CREATURE_MOVED:
				return canHandle || DataManager.NPC_SHOUT_DATA.hasAnyShout(getOwner().getWorldId(), getOwner().getNpcId(), ShoutEventType.SEE);
			case CREATURE_NEEDS_SUPPORT:
				return canHandle && isNonFightingState() && DataManager.TRIBE_RELATIONS_DATA.hasSupportRelations(getOwner().getTribe());
		}
		return canHandle;
	}

	@Override
	public AttackIntention chooseAttackIntention() {
		VisibleObject currentTarget = getTarget();
		Creature mostHated = getAggroList().getMostHated();

		if (mostHated == null || mostHated.getLifeStats().isAlreadyDead())
			return AttackIntention.FINISH_ATTACK;

		if (currentTarget == null)
			onCreatureEvent(AIEventType.TARGET_CHANGED, mostHated);

		if (getOwner().getObjectTemplate().getAttackRange() == 0) {
			NpcSkillEntry skill = getOwner().getSkillList().getRandomSkill();
			if (skill != null) {
				skillId = skill.getSkillId();
				skillLevel = skill.getSkillLevel();
				return AttackIntention.SKILL_ATTACK;
			}
		} else {
			NpcSkillEntry skill = SkillAttackManager.chooseNextSkill(this);
			if (skill != null) {
				skillId = skill.getSkillId();
				skillLevel = skill.getSkillLevel();
				return AttackIntention.SKILL_ATTACK;
			}
		}

		return AttackIntention.SIMPLE_ATTACK;
	}
}
