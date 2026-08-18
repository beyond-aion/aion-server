package com.aionemu.gameserver.controllers.movement;

/**
 * Tracks activation and deactivation of movement modifiers (the direction arrows shown next to the buff bar).
 * <p>
 * The whole mechanism is two slots:
 * <ul>
 * <li><b>pending</b> — the direction currently moved in, together with the time it started. It becomes effective once it has been held for more than
 * {@value #ACTIVATION_DELAY} ms, so movement shorter than that never applies anything and progress cannot accumulate over interruptions.
 * <li><b>confirmed</b> — the direction moved in before, but only if it had already become effective. It keeps being applied for another
 * {@value #DEACTIVATION_DELAY} ms, which is why the client shows several arrows at once while changing direction.
 * </ul>
 * The effective move state is the bit-wise or of both slots, so <b>at most two directions apply at the same time</b> and neither the state machine nor
 * the modifier table can ever see all three at once.
 * <p>
 * All state is time based, so no periodic task is needed: the current state is derived on demand in {@link #getMoveState()}.
 * <p>
 * There is no idle timeout — if move packets simply stop arriving, the pending direction stays effective. That cannot normally happen, because the
 * client always sends a stop packet and the state is also reset whenever a movement is rejected.
 */
public class MovementModifierState {

	/** Movement in the same direction needed before its modifier applies, exclusive. */
	static final int ACTIVATION_DELAY = 666;
	/** Time an already applied modifier keeps being applied after the direction changed, exclusive. */
	static final int DEACTIVATION_DELAY = 1332;

	private int pendingState = MoveState.NONE;
	private long pendingSince;
	private int confirmedState = MoveState.NONE;
	private long confirmedSince;

	/**
	 * @param moveState The direction currently moved in as a single {@link MoveState} flag, or {@link MoveState#NONE} when the movement ended.
	 */
	public void setMoveState(int moveState) {
		setMoveState(moveState, System.currentTimeMillis());
	}

	void setMoveState(int moveState, long now) {
		if (pendingState == moveState) // continued movement in the same direction never restarts the timer
			return;
		if (pendingState != MoveState.NONE && now - pendingSince > ACTIVATION_DELAY) {
			// the outgoing direction had become effective, so it now starts fading out. Movement too short for that is discarded silently.
			confirmedState = pendingState;
			confirmedSince = now;
		}
		pendingState = moveState;
		pendingSince = now;
	}

	/**
	 * @return The currently applied directions as a {@link MoveState} bit mask.
	 */
	public int getMoveState() {
		return getMoveState(System.currentTimeMillis());
	}

	int getMoveState(long now) {
		int moveState = MoveState.NONE;
		if (confirmedState != MoveState.NONE && now - confirmedSince < DEACTIVATION_DELAY)
			moveState = confirmedState;
		if (pendingState != MoveState.NONE && now - pendingSince > ACTIVATION_DELAY)
			moveState |= pendingState;
		return moveState;
	}

	@Override
	public String toString() {
		long now = System.currentTimeMillis();
		StringBuilder sb = new StringBuilder("[moveState=").append(getMoveState(now));
		sb.append(", pending=").append(pendingState).append(" since ").append(now - pendingSince).append("ms");
		if (confirmedState != MoveState.NONE && now - confirmedSince < DEACTIVATION_DELAY)
			sb.append(", ").append(confirmedState).append(" fades out in ").append(DEACTIVATION_DELAY - (now - confirmedSince)).append("ms");
		return sb.append(']').toString();
	}

	/**
	 * The simultaneously active directions as a bit mask, which indexes the modifier table in
	 * {@link com.aionemu.gameserver.utils.stats.StatFunctions#adjustStatByMovementModifier}. Left and right share the sideways bit, so switching
	 * between them is not a direction change and does not restart the activation timer.
	 */
	public static final class MoveState {

		public static final int NONE = 0;
		public static final int FORWARD = 1;
		public static final int BACKWARD = 2;
		public static final int SIDEWAYS = 4;
		public static final int COUNT = 8;

		private MoveState() {
		}
	}
}
