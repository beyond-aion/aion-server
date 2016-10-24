package com.aionemu.gameserver.ai2.manager;

import java.util.List;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.ai2.AIState;
import com.aionemu.gameserver.ai2.AISubState;
import com.aionemu.gameserver.ai2.NpcAI2;
import com.aionemu.gameserver.configs.main.AIConfig;
import com.aionemu.gameserver.configs.main.GeoDataConfig;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.geoEngine.collision.CollisionIntention;
import com.aionemu.gameserver.geoEngine.math.Vector3f;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.templates.walker.RouteStep;
import com.aionemu.gameserver.model.templates.walker.WalkerTemplate;
import com.aionemu.gameserver.utils.MathUtil;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.world.geo.GeoService;

/**
 * @author ATracer
 */
public class WalkManager {

	public static byte RANDOM_WALK_GEO_FLAGS = (byte) (CollisionIntention.PHYSICAL.getId() | CollisionIntention.DOOR.getId()
		| CollisionIntention.WALK.getId());

	/**
	 * @param npcAI
	 * @return True, if the npc started walking. False if walking is disabled, not supported, or the npc is already walking.
	 */
	public static boolean startWalking(NpcAI2 npcAI) {
		if (!AIConfig.ACTIVE_NPC_MOVEMENT)
			return false;
		return startRandomWalking(npcAI) || startRouteWalking(npcAI);
	}

	private static boolean startRandomWalking(NpcAI2 npcAI) {
		if (!npcAI.getOwner().isRandomWalker())
			return false;
		if (!npcAI.setStateIfNot(AIState.WALKING) || !npcAI.setSubStateIfNot(AISubState.WALK_RANDOM))
			return false;
		EmoteManager.emoteStartWalking(npcAI.getOwner());
		chooseNextRandomPoint(npcAI);
		return true;
	}

	private static boolean startRouteWalking(NpcAI2 npcAI) {
		Npc owner = npcAI.getOwner();
		if (!owner.isPathWalker())
			return false;
		WalkerTemplate template = DataManager.WALKER_DATA.getWalkerTemplate(owner.getSpawn().getWalkerId());
		if (template == null)
			return false;
		if (!npcAI.setStateIfNot(AIState.WALKING) || !npcAI.setSubStateIfNot(AISubState.WALK_PATH))
			return false;
		List<RouteStep> route = template.getRouteSteps();
		int currentPoint = owner.getMoveController().getCurrentPoint();
		RouteStep nextStep = findNextRoutStep(owner, route);
		owner.getMoveController().setCurrentRoute(route);
		owner.getMoveController().setRouteStep(nextStep, route.get(currentPoint));
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
	public static void startForcedWalking(NpcAI2 npcAI, float x, float y, float z) {
		npcAI.setStateIfNot(AIState.FORCED_WALKING);
		npcAI.setSubStateIfNot(AISubState.NONE);
		EmoteManager.emoteStartWalking(npcAI.getOwner());
		npcAI.getOwner().getMoveController().forcedMoveToPoint(x, y, z);
	}

	/**
	 * @param owner
	 * @param route
	 * @return
	 */
	protected static RouteStep findNextRoutStep(Npc owner, List<RouteStep> route) {
		int currentPoint = owner.getMoveController().getCurrentPoint();
		RouteStep nextStep = null;
		if (currentPoint != 0) {
			nextStep = findNextRouteStepAfterPause(owner, route, currentPoint);
		} else {
			nextStep = findClosestRouteStep(owner, route, nextStep);
		}
		return nextStep;
	}

	/**
	 * @param owner
	 * @param route
	 * @param nextStep
	 * @return
	 */
	protected static RouteStep findClosestRouteStep(Npc owner, List<RouteStep> route, RouteStep nextStep) {
		double closestDist = 0;
		float x = owner.getX();
		float y = owner.getY();
		float z = owner.getZ();

		if (owner.getWalkerGroup() != null) {
			// always choose the 1st step, not the last which is close enough
			if (owner.getWalkerGroup().getGroupStep() < 2)
				nextStep = route.get(0);
			else
				nextStep = route.get(owner.getWalkerGroup().getGroupStep() - 1);
		} else {
			for (RouteStep step : route) {
				double stepDist = MathUtil.getDistance(x, y, z, step.getX(), step.getY(), step.getZ());
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
	 * @param currentPoint
	 * @return
	 */
	protected static RouteStep findNextRouteStepAfterPause(Npc owner, List<RouteStep> route, int currentPoint) {
		RouteStep nextStep = route.get(currentPoint);
		double stepDist = MathUtil.getDistance(owner.getX(), owner.getY(), owner.getZ(), nextStep.getX(), nextStep.getY(), nextStep.getZ());
		if (stepDist < 1) {
			nextStep = nextStep.getNextStep();
		}
		return nextStep;
	}

	/**
	 * @param npcAI
	 */
	public static void targetReached(final NpcAI2 npcAI) {
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
	protected static void chooseNextRouteStep(final NpcAI2 npcAI) {
		int walkPause = npcAI.getOwner().getMoveController().getWalkPause();
		if (walkPause == 0) {
			npcAI.getOwner().getMoveController().resetMove();
			if (npcAI.getOwner().getMoveController().isNextRouteStepChosen())
				npcAI.getOwner().getMoveController().moveToNextPoint();
		} else {
			npcAI.getOwner().getMoveController().abortMove();
			npcAI.getOwner().getMoveController().isNextRouteStepChosen();
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

	/**
	 * @param npcAI
	 */
	private static void chooseNextRandomPoint(NpcAI2 npcAI) {
		Npc owner = npcAI.getOwner();
		owner.getMoveController().abortMove();

		ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				if (!npcAI.isInState(AIState.WALKING))
					return;
				int randomWalkRange = owner.getSpawn().getRandomWalkRange();
				int diameter = randomWalkRange * 2;
				float nextX = (Rnd.get() * diameter - randomWalkRange) + owner.getSpawn().getX();
				float nextY = (Rnd.get() * diameter - randomWalkRange) + owner.getSpawn().getY();
				if (GeoDataConfig.GEO_ENABLE && GeoDataConfig.GEO_NPC_MOVE) {
					Vector3f loc = GeoService.getInstance().getClosestCollision(owner, nextX, nextY, owner.getZ(), true, RANDOM_WALK_GEO_FLAGS);
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
	public static void stopWalking(NpcAI2 npcAI) {
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
	public static boolean isArrivedAtPoint(NpcAI2 npcAI) {
		return npcAI.getOwner().getMoveController().isReachedPoint();
	}
}
