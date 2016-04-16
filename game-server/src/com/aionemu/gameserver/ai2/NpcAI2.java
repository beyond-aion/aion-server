package com.aionemu.gameserver.ai2;

import com.aionemu.gameserver.ai2.event.AIEventType;
import com.aionemu.gameserver.ai2.event.AIListenable;
import com.aionemu.gameserver.ai2.handler.ActivateEventHandler;
import com.aionemu.gameserver.ai2.handler.CreatureEventHandler;
import com.aionemu.gameserver.ai2.handler.DiedEventHandler;
import com.aionemu.gameserver.ai2.handler.FollowEventHandler;
import com.aionemu.gameserver.ai2.handler.MoveEventHandler;
import com.aionemu.gameserver.ai2.handler.ShoutEventHandler;
import com.aionemu.gameserver.ai2.handler.SpawnEventHandler;
import com.aionemu.gameserver.ai2.manager.SimpleAttackManager;
import com.aionemu.gameserver.ai2.manager.WalkManager;
import com.aionemu.gameserver.ai2.poll.AIQuestion;
import com.aionemu.gameserver.configs.main.AIConfig;
import com.aionemu.gameserver.controllers.attack.AggroList;
import com.aionemu.gameserver.controllers.effect.EffectController;
import com.aionemu.gameserver.controllers.movement.NpcMoveController;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.TribeClass;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.skill.NpcSkillList;
import com.aionemu.gameserver.model.stats.container.NpcLifeStats;
import com.aionemu.gameserver.model.templates.npc.NpcTemplate;
import com.aionemu.gameserver.model.templates.spawns.SpawnTemplate;
import com.aionemu.gameserver.services.NpcShoutsService;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.utils.MathUtil;
import com.aionemu.gameserver.world.knownlist.KnownList;

/**
 * @author ATracer
 */
@AIName("npc")
public class NpcAI2 extends AITemplate {

	@Override
	public Npc getOwner() {
		return (Npc) super.getOwner();
	}

	protected NpcTemplate getObjectTemplate() {
		return getOwner().getObjectTemplate();
	}

	protected SpawnTemplate getSpawnTemplate() {
		return getOwner().getSpawn();
	}

	protected NpcLifeStats getLifeStats() {
		return getOwner().getLifeStats();
	}

	protected Race getRace() {
		return getOwner().getRace();
	}

	protected TribeClass getTribe() {
		return getOwner().getTribe();
	}

	protected EffectController getEffectController() {
		return getOwner().getEffectController();
	}

	protected KnownList getKnownList() {
		return getOwner().getKnownList();
	}

	protected AggroList getAggroList() {
		return getOwner().getAggroList();
	}

	protected NpcSkillList getSkillList() {
		return getOwner().getSkillList();
	}

	protected VisibleObject getCreator() {
		return getOwner().getCreator();
	}

	/**
	 * DEPRECATED as movements will be processed as commands only from ai
	 */
	protected NpcMoveController getMoveController() {
		return getOwner().getMoveController();
	}

	protected int getNpcId() {
		return getOwner().getNpcId();
	}

	protected int getCreatorId() {
		return getOwner().getCreatorId();
	}

	protected boolean isInRange(VisibleObject object, int range) {
		return MathUtil.isIn3dRange(getOwner(), object, range);
	}

	@Override
	@AIListenable(type = AIEventType.ACTIVATE)
	protected void handleActivate() {
		ActivateEventHandler.onActivate(this);
	}

	@Override
	@AIListenable(type = AIEventType.DEACTIVATE)
	protected void handleDeactivate() {
		ActivateEventHandler.onDeactivate(this);
	}

	@Override
	@AIListenable(type = AIEventType.BEFORE_SPAWNED)
	protected void handleBeforeSpawned() {
		SpawnEventHandler.onRespawn(this);
		if (getSkillList().getUseInSpawnedSkill() != null) {
			int skillId = getSkillList().getUseInSpawnedSkill().getSkillId();
			int skillLevel = getSkillList().getSkillLevel(skillId);
			AI2Actions.targetSelf(this);
			SkillEngine.getInstance().getSkill(getOwner(), skillId, skillLevel, getOwner()).useNoAnimationSkill();
		}
	}

	@Override
	@AIListenable(type = AIEventType.SPAWNED)
	protected void handleSpawned() {
		SpawnEventHandler.onSpawn(this);
		if (getSkillList().getUseInSpawnedSkill() != null) {
			int skillId = getSkillList().getUseInSpawnedSkill().getSkillId();
			int skillLevel = getSkillList().getSkillLevel(skillId);
			AI2Actions.targetSelf(this);
			SkillEngine.getInstance().getSkill(getOwner(), skillId, skillLevel, getOwner()).useNoAnimationSkill();
		}
		ShoutEventHandler.onSpawn(this);
	}

	@Override
	@AIListenable(type = AIEventType.DESPAWNED)
	protected void handleDespawned() {
		ShoutEventHandler.onBeforeDespawn(this);
		SpawnEventHandler.onDespawn(this);
	}

	@Override
	@AIListenable(type = AIEventType.DIED)
	protected void handleDied() {
		DiedEventHandler.onSimpleDie(this);
	}

	@Override
	@AIListenable(type = AIEventType.MOVE_ARRIVED)
	protected void handleMoveArrived() {
		ShoutEventHandler.onReachedWalkPoint(this);
	}

	@Override
	@AIListenable(type = AIEventType.TARGET_CHANGED)
	protected void handleTargetChanged(Creature creature) {
		ShoutEventHandler.onSwitchedTarget(this, creature);
	}

	@Override
	public boolean ask(AIQuestion question) {
		switch (question) {
			case DESTINATION_REACHED:
				return isDestinationReached();
			case CAN_SHOUT:
				return AIConfig.SHOUTS_ENABLE && NpcShoutsService.getInstance().mayShout(getOwner());
			case SHOULD_DECAY:
			case SHOULD_RESPAWN:
			case SHOULD_REWARD:
			case SHOULD_LOOT:
				return true;
			default:
				return false;
		}
	}

	protected boolean isDestinationReached() {
		AIState state = getState();
		switch (state) {
			case FEAR:
				return MathUtil.isNearCoordinates(getOwner(), getOwner().getMoveController().getTargetX2(), getOwner().getMoveController().getTargetY2(),
					getOwner().getMoveController().getTargetZ2(), 1);
			case FIGHT:
				return SimpleAttackManager.isTargetInAttackRange(getOwner());
			case RETURNING:
				SpawnTemplate spawn = getOwner().getSpawn();
				return MathUtil.isNearCoordinates(getOwner(), spawn.getX(), spawn.getY(), spawn.getZ(), 1);
			case FOLLOWING:
				return FollowEventHandler.isInRange(this, getOwner().getTarget());
			case WALKING:
				return getSubState() == AISubState.TALK || WalkManager.isArrivedAtPoint(this);
		}
		return true;
	}

	@Override
	protected void handleMoveValidate() {
		MoveEventHandler.onMoveValidate(this);
	}

	@Override
	protected void handleCreatureMoved(Creature creature) {
		CreatureEventHandler.onCreatureMoved(this, creature);
	}

	public boolean isMoveSupported() {
		return getOwner().getGameStats().getMovementSpeedFloat() > 0 && !this.isInSubState(AISubState.FREEZE);
	}

	/**
	 * NCsoft uses different non visible npcs as a sensor to trigger different events
	 */
	public void handleCreatureDetected(Creature creature) {

	}
}
