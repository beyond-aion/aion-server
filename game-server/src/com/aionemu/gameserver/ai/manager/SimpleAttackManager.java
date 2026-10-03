package com.aionemu.gameserver.ai.manager;

import com.aionemu.gameserver.ai.AILogger;
import com.aionemu.gameserver.ai.AIState;
import com.aionemu.gameserver.ai.NpcAI;
import com.aionemu.gameserver.ai.event.AIEventType;
import com.aionemu.gameserver.controllers.attack.AggroTarget;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.animations.AttackAnimation;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.utils.PositionUtil;
import com.aionemu.gameserver.world.geo.GeoService;

/**
 * @author ATracer
 */
public class SimpleAttackManager {

	/**
	 * Attacks the target now, the attack decision has found the auto attack due
	 */
	public static void performAttack(NpcAI npcAI) {
		if (npcAI.isLogging()) {
			AILogger.info(npcAI, "performAttack");
		}
		npcAI.getOwner().getGameStats().cancelAttackTask();
		attackAction(npcAI);
	}

	public static boolean isTargetInAttackRange(Npc npc) {
		VisibleObject target = npc.getTarget();
		if (!(target instanceof Creature))
			return false;
		return PositionUtil.isInAttackRange(npc, (Creature) target, npc.getGameStats().getAttackRange().getCurrent() / 1000f);
	}

	protected static void attackAction(final NpcAI npcAI) {
		if (!npcAI.isInState(AIState.FIGHT)) {
			return;
		}
		if (npcAI.isLogging()) {
			AILogger.info(npcAI, "attackAction");
		}
		Npc npc = npcAI.getOwner();
		Creature mostHated = npc.getAggroList().getTarget(AggroTarget.MOST_HATED);
		if (mostHated != null && !mostHated.equals(npc.getTarget())) {
			npcAI.onCreatureEvent(AIEventType.TARGET_CHANGED, mostHated);
		} else if (!(npc.getTarget() instanceof Creature target) || target.isDead()) {
			npcAI.onGeneralEvent(AIEventType.TARGET_GIVEUP);
		} else if (!npc.canSee(target)) {
			npc.getController().abortCast();
			npcAI.onGeneralEvent(AIEventType.TARGET_TOOFAR);
		} else if (!isTargetInAttackRange(npc)) {
			npcAI.onGeneralEvent(AIEventType.TARGET_TOOFAR);
		} else if (!GeoService.getInstance().canSee(npc, target)) { // delete geo check when we've implemented a pathfinding system
			npc.getController().cancelCurrentSkill(null);
			if (((System.currentTimeMillis() - npc.getMoveController().getLastMoveUpdate()) > 15000)
				&& npc.getGameStats().getLastAttackedTimeDelta() > 15) {
				npcAI.onGeneralEvent(AIEventType.TARGET_GIVEUP);
			} else {
				npcAI.onGeneralEvent(AIEventType.ATTACK_COMPLETE);
			}
		} else {
			if (!npc.getGameStats().tryStartAutoAttack()) { // not due yet, e.g. slowed while waiting, or another attack came first
				npc.getGameStats().scheduleAttackTask(() -> attackAction(npcAI), Math.max(1, npc.getGameStats().getNextAttackInterval()));
				return;
			}
			if (npc.isSpawned() && !npc.isDead() && !npc.getLifeStats().isAboutToDie() && npc.canAttack()) {
				npc.getPosition().setH(PositionUtil.getHeadingTowards(npc, target));
				int animation = DataManager.MOTION_DATA.chooseNpcAutoAttackAnimation(npc);
				int hitTime = DataManager.MOTION_DATA.calculateNpcAutoAttackHitTime(npc, target, animation);
				npc.getController().attackTarget(target, hitTime, null, AttackAnimation.getById(animation), true);
				npc.getGameStats().setAnimationEndTime(System.currentTimeMillis() + DataManager.MOTION_DATA.calculateNpcAutoAttackRecoveryMillis(npc));
			}
			npcAI.onGeneralEvent(AIEventType.ATTACK_COMPLETE);
		}
	}

}
