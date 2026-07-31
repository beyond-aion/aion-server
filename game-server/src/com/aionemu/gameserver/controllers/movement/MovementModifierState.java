package com.aionemu.gameserver.controllers.movement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tracks activation and deactivation of movement modifiers (the direction arrows shown next to the buff bar).
 * <p>
 * Retail behavior, based on frame by frame analysis of client recordings (see <a href="https://github.com/beyond-aion/aion-server/issues/102">#102</a>):
 * <ul>
 * <li>A direction is only confirmed after {@value #ACTIVATION_DELAY} ms of uninterrupted movement in it. Progress does not accumulate over
 * interruptions, so any direction change or stop before that restarts the timer from scratch.
 * <li>A confirmed direction stays active for another {@value #DEACTIVATION_DELAY} ms after movement in it ended, no matter what happens in between.
 * That's why the client shows several arrows at once while changing directions.
 * <li>While several directions are active, the modifiers of the highest priority one apply: forward, then sideways, then backward (verified via damage
 * output on retail).
 * <li>Left and right are tracked separately although they apply the same modifier, since frequently switching between them prevents activation.
 * <li>Jumping into a direction activates its modifiers immediately on landing, without waiting for the activation delay. Directions which are already
 * active are not affected by that.
 * </ul>
 * All state changes are time based, therefore no periodic task is needed: the current state is derived on demand in
 * {@link #getModifierDirection()}.
 */
public class MovementModifierState {

	private static final Logger log = LoggerFactory.getLogger(MovementModifierState.class);

	/**
	 * Temporary switch for diagnosing retail movement modifier behavior. Logs every state change with the prefix {@code MOVEDEBUG}, as well as the
	 * incoming move packets and the modifiers applied to attack damage. Must be disabled (or the logging removed) before release.
	 */
	public static final boolean DEBUG = true;

	/** Uninterrupted movement in the same direction needed to activate its modifier (observed: 838, 854, 863, 870, 879, 882 ms). */
	private static final int ACTIVATION_DELAY = 850;
	/** Time an activated modifier keeps being applied after movement in its direction ended (observed: 1350, 1367, 1384 ms). */
	private static final int DEACTIVATION_DELAY = 1350;
	/**
	 * Safety net in case we never learn about the end of a movement (packet loss, connection loss): movement is assumed to have ended this long after the
	 * last update. The game client sends position updates about every 672 ms while moving.
	 */
	private static final int MOVEMENT_UPDATE_TIMEOUT = 1000;

	/** Expiration times of directions which are no longer moved in, indexed by {@link Direction#ordinal()} (0 = inactive). */
	private final long[] activeUntil = new long[Direction.VALUES.length];
	private Direction currentDirection;
	private long movingSince;
	private long lastMovementUpdate;

	public void onMove(Direction direction) {
		String stateBefore = DEBUG ? toString() : null;
		onMove(direction, System.currentTimeMillis());
		if (DEBUG)
			logStateChange("onMove(" + direction + ")", stateBefore);
	}

	void onMove(Direction direction, long now) {
		lastMovementUpdate = now;
		if (direction == currentDirection)
			return; // keep the running activation timer, movement is uninterrupted
		endCurrentMovement(now);
		currentDirection = direction;
		movingSince = now;
	}

	public void onStop() {
		String stateBefore = DEBUG ? toString() : null;
		onStop(System.currentTimeMillis());
		if (DEBUG)
			logStateChange("onStop", stateBefore);
	}

	void onStop(long now) {
		endCurrentMovement(now);
	}

	/**
	 * Activates the current direction without waiting for the remaining activation delay. Used on landing, since jumping into a direction applies its
	 * modifiers immediately. Directions which are already active keep their original activation time, so their fade out is never shortened.
	 */
	public void commitCurrentDirection() {
		String stateBefore = DEBUG ? toString() : null;
		commitCurrentDirection(System.currentTimeMillis());
		if (DEBUG)
			logStateChange("commitCurrentDirection (landed)", stateBefore);
	}

	void commitCurrentDirection(long now) {
		if (currentDirection != null)
			movingSince = Math.min(movingSince, now - ACTIVATION_DELAY);
	}

	private void endCurrentMovement(long now) {
		if (currentDirection == null)
			return;
		if (now - movingSince >= ACTIVATION_DELAY) // unconfirmed movement is discarded and doesn't activate anything
			activeUntil[currentDirection.ordinal()] = now + DEACTIVATION_DELAY;
		currentDirection = null;
	}

	public MovementModifierDirection getModifierDirection() {
		return getModifierDirection(System.currentTimeMillis());
	}

	MovementModifierDirection getModifierDirection(long now) {
		MovementModifierDirection result = MovementModifierDirection.NONE;
		for (Direction direction : Direction.VALUES) {
			if (isActive(direction, now) && direction.modifierDirection.ordinal() > result.ordinal())
				result = direction.modifierDirection;
		}
		return result;
	}

	private boolean isActive(Direction direction, long now) {
		if (direction == currentDirection) {
			long movementEnd = Math.min(now, lastMovementUpdate + MOVEMENT_UPDATE_TIMEOUT);
			if (movementEnd - movingSince >= ACTIVATION_DELAY && movementEnd + DEACTIVATION_DELAY > now)
				return true;
		}
		// a direction may still fade out from a previous activation, even while it's already moved in again
		return activeUntil[direction.ordinal()] > now;
	}

	private void logStateChange(String event, String stateBefore) {
		String stateAfter = toString();
		if (!stateAfter.equals(stateBefore))
			log.info("MOVEDEBUG state {}: {} => {}", event, stateBefore, stateAfter);
	}

	@Override
	public String toString() {
		long now = System.currentTimeMillis();
		StringBuilder sb = new StringBuilder("[applied=").append(getModifierDirection(now));
		if (currentDirection == null)
			sb.append(", notMoving");
		else
			sb.append(", moving ").append(currentDirection).append(" since ").append(now - movingSince).append("ms, last update ")
				.append(now - lastMovementUpdate).append("ms ago");
		for (Direction direction : Direction.VALUES) {
			if (activeUntil[direction.ordinal()] > now)
				sb.append(", ").append(direction).append(" fades out in ").append(activeUntil[direction.ordinal()] - now).append("ms");
		}
		return sb.append(']').toString();
	}

	public enum Direction {
		FORWARD(MovementModifierDirection.FORWARD),
		BACKWARD(MovementModifierDirection.BACKWARD),
		LEFT(MovementModifierDirection.SIDEWAYS),
		RIGHT(MovementModifierDirection.SIDEWAYS);

		private static final Direction[] VALUES = values();

		private final MovementModifierDirection modifierDirection;

		Direction(MovementModifierDirection modifierDirection) {
			this.modifierDirection = modifierDirection;
		}

		public MovementModifierDirection getModifierDirection() {
			return modifierDirection;
		}
	}
}
