package com.aionemu.gameserver.ai.manager;

import java.util.List;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.ai.AIState;
import com.aionemu.gameserver.ai.AISubState;
import com.aionemu.gameserver.ai.NpcAI;
import com.aionemu.gameserver.configs.main.AIConfig;
import com.aionemu.gameserver.configs.main.GeoDataConfig;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.geoEngine.collision.CollisionIntention;
import com.aionemu.gameserver.geoEngine.collision.IgnoreProperties;
import com.aionemu.gameserver.geoEngine.math.Vector3f;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.templates.walker.RouteStep;
import com.aionemu.gameserver.model.templates.walker.WalkerTemplate;
import com.aionemu.gameserver.utils.PositionUtil;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.world.geo.GeoService;

/**
 * @author ATracer
 */
public class WalkManager {

	public static byte RANDOM_WALK_GEO_FLAGS = (byte) (CollisionIntention.CANT_SEE_COLLISIONS.getId() | CollisionIntention.WALK.getId() | CollisionIntention.PHYSICAL_SEE_THROUGH.getId());

	/**
	 * @return True, if the npc started walking. False if walking is disabled, not supported, or the npc is already walking.
	 */
	public static boolean startWalking(NpcAI npcAI) {
		if (!AIConfig.ACTIVE_NPC_MOVEMENT || !npcAI.getOwner().isSpawned())
			return false;
		return startRandomWalking(npcAI) || startRouteWalking(npcAI);
	}

	private static boolean startRandomWalking(NpcAI npcAI) {
		if (!npcAI.getOwner().isRandomWalker())
			return false;
		if (!npcAI.setStateIfNot(AIState.WALKING) || !npcAI.setSubStateIfNot(AISubState.WALK_RANDOM))
			return false;
		EmoteManager.emoteStartWalking(npcAI.getOwner());
		chooseNextRandomPoint(npcAI);
		return true;
	}

	private static boolean startRouteWalking(NpcAI npcAI) {
		Npc owner = npcAI.getOwner();
		if (!owner.isPathWalker())
			return false;
		if (owner.getMoveController().getWalkerTemplate() == null) {
			WalkerTemplate template = DataManager.WALKER_DATA.getWalkerTemplate(owner.getSpawn().getWalkerId());
			if (template == null)
				return false;
			owner.getMoveController().setWalkerTemplate(template, 0);
		}
		if (!npcAI.setStateIfNot(AIState.WALKING) || !npcAI.setSubStateIfNot(AISubState.WALK_PATH))
			return false;
		RouteStep nextStep = findNextRoutStep(owner);
		owner.getMoveController().setRouteStep(nextStep);
		EmoteManager.emoteStartWalking(npcAI.getOwner());
		npcAI.getOwner().getMoveController().moveToNextPoint();
		return true;
	}

	/**
	 * @param npcAI
	 * @param x
	 * @param y
	 * @param z
	 */
	public static void startForcedWalking(NpcAI npcAI, float x, float y, float z) {
		npcAI.setStateIfNot(AIState.FORCED_WALKING);
		npcAI.setSubStateIfNot(AISubState.NONE);
		EmoteManager.emoteStartWalking(npcAI.getOwner());
		npcAI.getOwner().getMoveController().forcedMoveToPoint(x, y, z);
	}

	/**
	 * @param owner
	 * @return
	 */
	protected static RouteStep findNextRoutStep(Npc owner) {
		RouteStep currentStep = owner.getMoveController().getCurrentStep();
		RouteStep nextStep = null;
		if (currentStep.getStepIndex() != 0) {
			nextStep = findNextRouteStepAfterPause(owner, currentStep);
		} else {
			nextStep = findClosestRouteStep(owner);
		}
		return nextStep;
	}

	/**
	 * @param owner
	 * @return
	 */
	protected static RouteStep findClosestRouteStep(Npc owner) {
		List<RouteStep> route = owner.getMoveController().getWalkerTemplate().getRouteSteps();
		RouteStep nextStep = null;
		if (owner.getWalkerGroup() != null) {
			nextStep = route.get(owner.getWalkerGroup().getGroupStep());
		} else {
			double closestDist = 0;
			float x = owner.getX();
			float y = owner.getY();
			float z = owner.getZ();
			for (RouteStep step : route) {
				double stepDist = PositionUtil.getDistance(x, y, z, step.getX(), step.getY(), step.getZ());
				if (closestDist == 0 || stepDist < closestDist) {
					closestDist = stepDist;
					nextStep = step;
				}
			}
		}
		return nextStep;
	}

	/**
	 * @param owner
	 * @param route
	 * @param currentStep
	 * @return
	 */
	protected static RouteStep findNextRouteStepAfterPause(Npc owner, RouteStep currentStep) {
		if (PositionUtil.isInRange(owner, currentStep.getX(), currentStep.getY(), currentStep.getZ(), 1)) {
			List<RouteStep> route = owner.getMoveController().getWalkerTemplate().getRouteSteps();
			if (currentStep.isLastStep())
				return route.get(0);
			else
				return route.get(currentStep.getStepIndex() + 1);
		}
		return currentStep;
	}

	/**
	 * @param npcAI
	 */
	public static void targetReached(final NpcAI npcAI) {
		if (npcAI.isInState(AIState.WALKING)) {
			switch (npcAI.getSubState()) {
				case WALK_PATH:
					npcAI.getOwner().updateKnownlist();
					if (npcAI.getOwner().getWalkerGroup() != null) {
						npcAI.getOwner().getWalkerGroup().targetReached(npcAI);
					} else {
						chooseNextRouteStep(npcAI);
					}
					break;
				case WALK_WAIT_GROUP:
					npcAI.setSubStateIfNot(AISubState.WALK_PATH);
					chooseNextRouteStep(npcAI);
					break;
				case WALK_RANDOM:
					chooseNextRandomPoint(npcAI);
					break;
				case TALK:
					npcAI.setStateIfNot(AIState.IDLE);
					npcAI.getOwner().getMoveController().abortMove();
					break;
			}
		} else if (npcAI.isInState(AIState.FORCED_WALKING)) {
			npcAI.getOwner().getMoveController().abortMove();
			npcAI.setStateIfNot(AIState.IDLE);
			npcAI.think();
		}
	}

	/**
	 * @param npcAI
	 */
	protected static void chooseNextRouteStep(NpcAI npcAI) {
		int walkPause = npcAI.getOwner().getMoveController().getCurrentStep().getRestTime();
		if (walkPause == 0) {
			npcAI.getOwner().getMoveController().resetMove();
			if (npcAI.getOwner().getMoveController().isNextRouteStepChosen())
				npcAI.getOwner().getMoveController().moveToNextPoint();
		} else {
			npcAI.getOwner().getMoveController().abortMove();
			if (npcAI.getOwner().getMoveController().isNextRouteStepChosen()) {
				ThreadPoolManager.getInstance().schedule(new Runnable() {

					@Override
					public void run() {
						if (npcAI.isInState(AIState.WALKING)) {
							npcAI.getOwner().getMoveController().moveToNextPoint();
						}
					}
				}, walkPause);
			}
		}
	}

	/**
	 * @param npcAI
	 */
	private static void chooseNextRandomPoint(NpcAI npcAI) {
		Npc owner = npcAI.getOwner();
		owner.getMoveController().abortMove();

		ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				if (!npcAI.isInState(AIState.WALKING))
					return;
				int randomWalkRange = owner.getSpawn().getRandomWalkRange();
				int diameter = randomWalkRange * 2;
				float nextX = Rnd.nextFloat(diameter) - randomWalkRange + owner.getSpawn().getX();
				float nextY = Rnd.nextFloat(diameter) - randomWalkRange + owner.getSpawn().getY();
				if (GeoDataConfig.GEO_ENABLE && GeoDataConfig.GEO_NPC_MOVE) {
					Vector3f loc = GeoService.getInstance().getClosestCollision(owner, nextX, nextY, owner.getZ(), true, RANDOM_WALK_GEO_FLAGS, IgnoreProperties.of(owner.getRace()));
					owner.getMoveController().moveToPoint(loc.x, loc.y, loc.z);
				} else {
					owner.getMoveController().moveToPoint(nextX, nextY, owner.getZ());
				}
			}
		}, Rnd.get(AIConfig.MINIMIMUM_DELAY, AIConfig.MAXIMUM_DELAY) * 1000);
	}

	/**
	 * @param npcAI
	 */
	public static void stopWalking(NpcAI npcAI) {
		npcAI.getOwner().getMoveController().abortMove();
		npcAI.setStateIfNot(AIState.IDLE);
		if (npcAI.getSubState() != AISubState.FREEZE)
			npcAI.setSubStateIfNot(AISubState.NONE);
		EmoteManager.emoteStopWalking(npcAI.getOwner());
	}

	/**
	 * @param npcAI
	 * @return
	 */
	public static boolean isArrivedAtPoint(NpcAI npcAI) {
		return npcAI.getOwner().getMoveController().isReachedPoint();
	}
}
