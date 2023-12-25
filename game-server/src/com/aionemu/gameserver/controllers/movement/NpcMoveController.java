package com.aionemu.gameserver.controllers.movement;

import java.util.List;

import com.aionemu.gameserver.ai.event.AIEventType;
import com.aionemu.gameserver.world.navmesh.NavMeshService;
import org.recast4j.detour.StraightPathItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.ai.AILogger;
import com.aionemu.gameserver.ai.AIState;
import com.aionemu.gameserver.ai.AISubState;
import com.aionemu.gameserver.ai.NpcAI;
import com.aionemu.gameserver.ai.handler.TargetEventHandler;
import com.aionemu.gameserver.ai.manager.WalkManager;
import com.aionemu.gameserver.configs.main.GeoDataConfig;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.gameobjects.state.CreatureState;
import com.aionemu.gameserver.model.geometry.Point3D;
import com.aionemu.gameserver.model.stats.calc.Stat2;
import com.aionemu.gameserver.model.templates.walker.RouteStep;
import com.aionemu.gameserver.model.templates.walker.WalkerTemplate;
import com.aionemu.gameserver.model.templates.walker.WalkerTemplate.LoopType;
import com.aionemu.gameserver.model.templates.zone.Point2D;
import com.aionemu.gameserver.network.aion.serverpackets.SM_MOVE;
import com.aionemu.gameserver.spawnengine.WalkerFormator;
import com.aionemu.gameserver.spawnengine.WalkerGroup;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.PositionUtil;
import com.aionemu.gameserver.utils.collections.LastUsedCache;
import com.aionemu.gameserver.world.World;
import com.aionemu.gameserver.world.geo.GeoService;

/**
 * @author ATracer
 */
public class NpcMoveController extends CreatureMoveController<Npc> {

	private static final Logger log = LoggerFactory.getLogger(NpcMoveController.class);
	private static final float MOVE_OFFSET = 0.05f;

	private Destination destination = Destination.TARGET_OBJECT;

	private float pointX;
	private float pointY;
	private float pointZ;
	private boolean isStop;

	private LastUsedCache<Byte, Point3D> lastSteps = null;
	private byte stepSequenceNr = 0;

	private WalkerTemplate walkerTemplate;
	private RouteStep currentStep;
	private float cachedTargetZ;

	private List<StraightPathItem> pathItems = null;
	private StraightPathItem currentPathItem = null;

	public NpcMoveController(Npc owner) {
		super(owner);
	}

	private enum Destination {
		TARGET_OBJECT,
		POINT,
		FORCED_POINT
	}

	/**
	 * Move to current target
	 */
	public void moveToTargetObject() {
		if (started.compareAndSet(false, true)) {
			if (owner.getAi().isLogging()) {
				AILogger.moveinfo(owner, "MC: moveToTarget started");
			}
			destination = Destination.TARGET_OBJECT;

			if (NavMeshService.getInstance().mapSupportsNavMesh(owner.getWorldId())) {
				calculateNavMeshPath();
			}
			updateLastMove();
			owner.getController().onStartMove();
		}
	}

	private void calculateNavMeshPath() {
		if (owner.getAi().isLogging()) {
			AILogger.moveinfo(owner, "MC: calculating navmesh path");
		}
		pathItems = NavMeshService.getInstance().calculatePath(owner, owner.getTarget());
		if (pathItems != null && !pathItems.isEmpty()) {
			pathItems.removeFirst();
		}
	}

	public void moveToPoint(float x, float y, float z) {
		if (started.compareAndSet(false, true)) {
			if (owner.getAi().isLogging()) {
				AILogger.moveinfo(owner, "MC: moveToPoint started");
			}
			destination = Destination.POINT;
			pointX = x;
			pointY = y;
			pointZ = z;
			updateLastMove();
			owner.getController().onStartMove();
		}
	}

	public void forcedMoveToPoint(float x, float y, float z) {
		if (started.compareAndSet(false, true)) {
			if (owner.getAi().isLogging()) {
				AILogger.moveinfo(owner, "MC: forcedMoveToPoint started");
			}
			destination = Destination.FORCED_POINT;
			pointX = x;
			pointY = y;
			pointZ = z;
			updateLastMove();
			owner.getController().onStartMove();
		}
	}

	public void moveToNextPoint() {
		if (started.compareAndSet(false, true)) {
			if (owner.getAi().isLogging()) {
				AILogger.moveinfo(owner, "MC: moveToNextPoint started");
			}
			destination = Destination.POINT;
			updateLastMove();
			owner.getController().onStartMove();
		}
	}

	/**
	 * @return if destination reached
	 */
	@Override
	public void moveToDestination() {
		if (owner.getAi().isLogging()) {
			AILogger.moveinfo(owner, "moveToDestination destination: " + destination);
		}
		if (owner.isDead()) {
			abortMove();
			return;
		}
		if (!owner.canPerformMove()) {
			if (owner.getAi().isLogging()) {
				AILogger.moveinfo(owner, "moveToDestination can't perform move");
			}
			if (started.compareAndSet(true, false)) {
				setAndSendStopMove(owner);
				updateLastMove();
			}
			return;
		} else if (started.compareAndSet(false, true)) {
			updateLastMove();
			setAndSendStartMove(owner);
		}

		if (!started.get()) {
			if (owner.getAi().isLogging()) {
				AILogger.moveinfo(owner, "moveToDestination not started");
			}
		}

		switch (destination) {
			case TARGET_OBJECT:
				VisibleObject target = owner.getTarget();// todo no target
				if (!(target instanceof Creature))
					return;
				if (NavMeshService.getInstance().mapSupportsNavMesh(owner.getWorldId())) {
					if (pathItems != null) {
						if (owner.getAi().isLogging()) {
							AILogger.moveinfo(owner, "moveToDestination navmesh path items not null, size: " + pathItems.size());
						}
						float[] lastPos = pathItems.isEmpty() ? (currentPathItem != null ? currentPathItem.getPos() : null) : pathItems.getLast().getPos();
						if (lastPos != null && !PositionUtil.isInRange(target, lastPos[2], lastPos[0], lastPos[1], MOVE_CHECK_OFFSET)) {
							if (owner.getAi().isLogging()) {
								AILogger.moveinfo(owner, "moveToDestination navmesh target moved");
							}
							calculateNavMeshPath();
							if (pathItems == null || pathItems.isEmpty()) {
								if (owner.getAi().isLogging()) {
									AILogger.moveinfo(owner, "moveToDestination navmesh recalculated null");
								}
								abortMove();
								return;
							} else {
								if (owner.getAi().isLogging()) {
									AILogger.moveinfo(owner, "moveToDestination navmesh recalculated, size:  " + pathItems.size());
								}
								currentPathItem = pathItems.removeFirst();
							}
						}
						if (currentPathItem == null) {
							if (!pathItems.isEmpty()) {
								if (owner.getAi().isLogging()) {
									AILogger.moveinfo(owner, "moveToDestination navmesh cur Path item null but pathItems not empty");
								}
								currentPathItem = pathItems.removeFirst();
							} else {
								if (owner.getAi().isLogging()) {
									AILogger.moveinfo(owner, "moveToDestination navmesh cur Path item null and pathItems empty, aborting");
								}
								abortMove();
								return;
							}
						}
						float[] dest = currentPathItem.getPos();
						if (PositionUtil.isInRange(owner, dest[2], dest[0], dest[1], MOVE_CHECK_OFFSET) && !pathItems.isEmpty()) {
							if (owner.getAi().isLogging()) {
								AILogger.moveinfo(owner, "moveToDestination navmesh cur Path item near, moving to next point");
							}
							currentPathItem = pathItems.removeFirst();
							dest = currentPathItem.getPos();
							pointX = dest[2];
							pointY = dest[0];
							pointZ = dest[1];
						} else {
							if (owner.getAi().isLogging()) {
								AILogger.moveinfo(owner, "moveToDestination navmesh cur Path item not near, continue on prev path");
							}
							pointX = dest[2];
							pointY = dest[0];
							pointZ = dest[1];
						}
					} else {
						if (owner.getAi().isLogging()) {
							AILogger.moveinfo(owner, "moveToDestination navmesh path items null ");
						}
						abortMove();
						owner.getAi().onGeneralEvent(AIEventType.TARGET_GIVEUP);
					}
				}
				else if (!PositionUtil.isInRange(target, pointX, pointY, pointZ, MOVE_CHECK_OFFSET)) {
					if (owner.getAi().isLogging()) {
						AILogger.moveinfo(owner, "moveToDestination target not in range of points");
					}
					Creature creature = (Creature) target;
					pointX = target.getX();
					pointY = target.getY();
					pointZ = getTargetZ(creature);
				}
				moveToLocation(pointX, pointY, pointZ);
				break;
			case POINT:
			case FORCED_POINT:
				moveToLocation(pointX, pointY, pointZ);
				break;
		}
		updateLastMove();
	}

	/**
	 * @param creature
	 * @return
	 */
	private float getTargetZ(Creature creature) {
		float targetZ = creature.getZ();
		if (GeoDataConfig.GEO_NPC_MOVE && creature.isInFlyingState() && !owner.isInFlyingState()) {
			if (owner.getGameStats().checkGeoNeedUpdate()) {
				float lowestZ = Math.min(creature.getZ(), owner.getZ());
				float geoZ = GeoService.getInstance().getZ(creature, creature.getZ() + 2, lowestZ - 5);
				if (!Float.isNaN(geoZ))
					cachedTargetZ = geoZ;
				else
					cachedTargetZ = lowestZ;
			}
			targetZ = cachedTargetZ;
		}
		return targetZ;
	}

	/**
	 * @param targetX
	 * @param targetY
	 * @param targetZ
	 * @return
	 */
	protected void moveToLocation(float targetX, float targetY, float targetZ) {
		float ownerX = owner.getX();
		float ownerY = owner.getY();
		float ownerZ = owner.getZ();
		boolean directionChanged = targetX != targetDestX || targetY != targetDestY || targetZ != targetDestZ;

		if (directionChanged) {
			heading = (byte) (Math.toDegrees(Math.atan2(targetY - ownerY, targetX - ownerX)) / 3);
		}

		if (owner.getAi().isLogging()) {
			AILogger.moveinfo(owner, "OLD targetDestX: " + targetDestX + " targetDestY: " + targetDestY + " targetDestZ " + targetDestZ);
		}

		// to prevent broken walkers in case of activating/deactivating zones
		if (targetX == 0 && targetY == 0) {
			targetX = owner.getSpawn().getX();
			targetY = owner.getSpawn().getY();
			targetZ = owner.getSpawn().getZ();
		}

		targetDestX = targetX;
		targetDestY = targetY;
		targetDestZ = targetZ;

		if (owner.getAi().isLogging()) {
			AILogger.moveinfo(owner, "ownerX=" + ownerX + " ownerY=" + ownerY + " ownerZ=" + ownerZ);
			AILogger.moveinfo(owner, "targetDestX: " + targetDestX + " targetDestY: " + targetDestY + " targetDestZ " + targetDestZ);
		}

		float currentSpeed = owner.getGameStats().getMovementSpeedFloat();
		float futureDistPassed = currentSpeed * (System.currentTimeMillis() - lastMoveUpdate) / 1000f;
		float dist = (float) PositionUtil.getDistance(ownerX, ownerY, ownerZ, targetX, targetY, targetZ);

		if (owner.getAi().isLogging()) {
			AILogger.moveinfo(owner, "futureDist: " + futureDistPassed + " dist: " + dist);
		}

		if (dist == 0) {
			if (owner.getAi().getState() == AIState.RETURNING) {
				if (owner.getAi().isLogging()) {
					AILogger.moveinfo(owner, "State RETURNING: abort move");
				}
				TargetEventHandler.onTargetReached((NpcAI) owner.getAi());
			}
			return;
		}

		if (futureDistPassed > dist) {
			futureDistPassed = dist;
		}

		float distFraction = futureDistPassed / dist;
		float newX = (targetDestX - ownerX) * distFraction + ownerX;
		float newY = (targetDestY - ownerY) * distFraction + ownerY;
		float newZ = (targetDestZ - ownerZ) * distFraction + ownerZ;
		if (GeoDataConfig.GEO_NPC_MOVE && GeoDataConfig.GEO_ENABLE && owner.getAi().getSubState() != AISubState.WALK_PATH
			&& owner.getAi().getState() != AIState.RETURNING && owner.getGameStats().getNextGeoZUpdate() < System.currentTimeMillis()) {
			// fix Z if npc doesn't move to spawn point
			if (owner.getSpawn().getX() != targetDestX || owner.getSpawn().getY() != targetDestY || owner.getSpawn().getZ() != targetDestZ) {
				float geoZ = GeoService.getInstance().getZ(owner.getWorldId(), newX, newY, newZ + 2, Math.min(newZ, ownerZ) - 2, owner.getInstanceId());
				if (!Float.isNaN(geoZ)) {
					if (Math.abs(newZ - geoZ) > 1)
						directionChanged = true;
					newZ = geoZ;
					boolean isXYDestinationReached = PositionUtil.getDistance(newX, newY, pointX, pointY) < MOVE_OFFSET;
					if (isXYDestinationReached && !PositionUtil.isInRange(newX, newY, newZ, pointX, pointY, pointZ, MOVE_OFFSET))
						pointZ = newZ; // original pointZ is unreachable, override it so isReachedPoint() can return true
				}
			}
			owner.getGameStats().setNextGeoZUpdate(System.currentTimeMillis() + 1000);
		}
		if (owner.getAi().isLogging()) {
			AILogger.moveinfo(owner, "newX=" + newX + " newY=" + newY + " newZ=" + newZ + " mask=" + movementMask);
		}

		World.getInstance().updatePosition(owner, newX, newY, newZ, heading, false);

		byte newMask = getMoveMask(directionChanged);
		if (movementMask != newMask || directionChanged) {
			if (movementMask != newMask) {
				if (owner.getAi().isLogging()) {
					AILogger.moveinfo(owner, "oldMask=" + movementMask + " newMask=" + newMask);
				}
				movementMask = newMask;
			}
			PacketSendUtility.broadcastPacket(owner, new SM_MOVE(owner));
		}
	}

	private byte getMoveMask(boolean directionChanged) {
		if (directionChanged)
			return MovementMask.NPC_STARTMOVE;
		else if (owner.getAi().getState() == AIState.RETURNING)
			return MovementMask.NPC_RUN_FAST;
		else if (owner.getAi().getState() == AIState.FOLLOWING)
			return MovementMask.NPC_WALK_SLOW;

		byte mask = MovementMask.IMMEDIATE;
		final Stat2 stat = owner.getGameStats().getMovementSpeed();
		if (owner.isInState(CreatureState.WEAPON_EQUIPPED)) {
			mask = stat.getBonus() < 0 ? MovementMask.NPC_RUN_FAST : MovementMask.NPC_RUN_SLOW;
		} else if (owner.isInState(CreatureState.WALK_MODE) || owner.isInState(CreatureState.ACTIVE)) {
			mask = stat.getBonus() < 0 ? MovementMask.NPC_WALK_FAST : MovementMask.NPC_WALK_SLOW;
		}
		if (owner.isFlying())
			mask |= MovementMask.GLIDE;
		return mask;
	}

	@Override
	public void abortMove() {
		if (!started.get())
			return;
		resetMove();
		setAndSendStopMove(owner);
	}

	/**
	 * Initialize values to default ones
	 */
	public void resetMove() {
		if (owner.getAi().isLogging()) {
			AILogger.moveinfo(owner, "MC perform stop");
		}
		owner.getController().onStopMove();
		started.set(false);
		targetDestX = 0;
		targetDestY = 0;
		targetDestZ = 0;
		pointX = 0;
		pointY = 0;
		pointZ = 0;
		pathItems = null;
		currentPathItem = null;
	}

	public WalkerTemplate getWalkerTemplate() {
		return walkerTemplate;
	}

	public void setWalkerTemplate(WalkerTemplate walkerTemplate, int stepIndex) {
		this.walkerTemplate = walkerTemplate;
		this.currentStep = walkerTemplate.getRouteStep(stepIndex);
	}

	public void setRouteStep(RouteStep step) {
		Point2D dest = null;
		if (owner.getWalkerGroup() != null) {
			dest = WalkerGroup.getLinePoint(new Point2D(currentStep.getX(), currentStep.getY()), new Point2D(step.getX(), step.getY()),
				owner.getWalkerGroupShift());
			this.pointZ = currentStep.getZ();
			owner.getWalkerGroup().setStep(owner, step.getStepIndex());
		} else {
			this.pointZ = step.getZ();
			this.isStop = walkerTemplate.getLoopType() == LoopType.NONE && step.isLastStep();
		}
		this.currentStep = step;
		this.pointX = dest == null ? step.getX() : dest.getX();
		this.pointY = dest == null ? step.getY() : dest.getY();
		this.destination = Destination.POINT;
	}

	public RouteStep getCurrentStep() {
		return currentStep;
	}

	public boolean isReachedPoint() {
		return PositionUtil.isInRange(owner, pointX, pointY, pointZ, MOVE_OFFSET);
	}

	public boolean isNextRouteStepChosen() {
		if (isStop) {
			WalkManager.stopWalking((NpcAI) owner.getAi());
			return false;
		}
		if (walkerTemplate == null) {
			WalkManager.stopWalking((NpcAI) owner.getAi());
			if (WalkerFormator.processClusteredNpc(owner, owner.getWorldId(), owner.getInstanceId()))
				return false;

			setWalkerTemplate(DataManager.WALKER_DATA.getWalkerTemplate(owner.getSpawn().getWalkerId()), 0);
			if (walkerTemplate == null) {
				log.warn("Bad Walker Id: " + owner.getSpawn().getWalkerId() + " - point: " + currentStep.getStepIndex());
				return false;
			}
		}
		List<RouteStep> routeSteps = walkerTemplate.getRouteSteps();
		RouteStep nextStep = currentStep.isLastStep() ? routeSteps.get(0) : routeSteps.get(currentStep.getStepIndex() + 1);
		setRouteStep(nextStep);
		return true;
	}

	public boolean isChangingDirection() {
		return currentStep.getStepIndex() == 0;
	}

	@Override
	public final float getTargetX2() {
		return started.get() ? targetDestX : owner.getX();
	}

	@Override
	public final float getTargetY2() {
		return started.get() ? targetDestY : owner.getY();
	}

	@Override
	public final float getTargetZ2() {
		return started.get() ? targetDestZ : owner.getZ();
	}

	public boolean isStop() {
		return isStop;
	}

	/**
	 * @return
	 */
	public boolean isFollowingTarget() {
		return destination == Destination.TARGET_OBJECT;
	}

	public synchronized void storeStep() {
		if (owner.getAi().getState() == AIState.RETURNING)
			return;
		if (lastSteps == null)
			lastSteps = new LastUsedCache<>(10);
		Point3D currentStep = new Point3D(owner.getX(), owner.getY(), owner.getZ());
		if (owner.getAi().isLogging()) {
			AILogger.moveinfo(owner, "store back step: X=" + owner.getX() + " Y=" + owner.getY() + " Z=" + owner.getZ());
		}
		if (stepSequenceNr == 0 || PositionUtil.getDistance(lastSteps.get(stepSequenceNr), currentStep) >= 10)
			lastSteps.put(++stepSequenceNr, currentStep);
	}

	public synchronized Point3D recallPreviousStep() {
		if (lastSteps == null)
			lastSteps = new LastUsedCache<>(10);

		Point3D result = stepSequenceNr == 0 ? null : lastSteps.get(stepSequenceNr--);

		if (result == null) {
			if (owner.getAi().isLogging())
				AILogger.moveinfo(owner, "recall back step: spawn point");
			targetDestX = owner.getSpawn().getX();
			targetDestY = owner.getSpawn().getY();
			targetDestZ = owner.getSpawn().getZ();
			result = new Point3D(targetDestX, targetDestY, targetDestZ);
		} else {
			if (owner.getAi().isLogging())
				AILogger.moveinfo(owner, "recall back step: X=" + result.getX() + " Y=" + result.getY() + " Z=" + result.getZ());
			targetDestX = result.getX();
			targetDestY = result.getY();
			targetDestZ = result.getZ();
		}

		return result;
	}

	public synchronized void clearBackSteps() {
		stepSequenceNr = 0;
		lastSteps = null;
		movementMask = MovementMask.IMMEDIATE;
	}
}
