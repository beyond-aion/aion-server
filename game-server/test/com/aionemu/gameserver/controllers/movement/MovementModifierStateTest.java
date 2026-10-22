package com.aionemu.gameserver.controllers.movement;

import static com.aionemu.gameserver.controllers.movement.MovementModifierState.MoveState.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/**
 * Tests the activation and deactivation timing of the movement modifier state machine.
 */
class MovementModifierStateTest {

	private final MovementModifierState state = new MovementModifierState();

	@Test
	void testActivationDelay() {
		state.setMoveState(FORWARD, 0);
		assertEquals(NONE, state.getMoveState(0));
		assertEquals(NONE, state.getMoveState(666)); // the delay must be exceeded, not just reached
		assertEquals(FORWARD, state.getMoveState(667));
	}

	@Test
	void testModifierStaysActiveWhileMoving() {
		for (long now = 0; now <= 100_000; now += 500) // the client sends position updates about every 672ms while moving
			state.setMoveState(FORWARD, now);
		assertEquals(FORWARD, state.getMoveState(100_000));
	}

	@Test
	void testContinuedMovementDoesNotRestartTheTimer() {
		state.setMoveState(FORWARD, 0);
		state.setMoveState(FORWARD, 600); // same direction, so the activation timer keeps running
		assertEquals(FORWARD, state.getMoveState(667));
	}

	@Test
	void testDeactivationDelay() {
		state.setMoveState(BACKWARD, 0);
		state.setMoveState(NONE, 1000); // activated, so it starts fading out
		assertEquals(BACKWARD, state.getMoveState(1000));
		assertEquals(BACKWARD, state.getMoveState(2331));
		assertEquals(NONE, state.getMoveState(2332));
	}

	@Test
	void testMovementTooShortToActivateIsDiscarded() {
		state.setMoveState(FORWARD, 0);
		state.setMoveState(NONE, 666); // interrupted before activation, so nothing is carried over
		assertEquals(NONE, state.getMoveState(666));
		assertEquals(NONE, state.getMoveState(5000));
	}

	@Test
	void testActivationProgressDoesNotAccumulate() {
		state.setMoveState(FORWARD, 0);
		state.setMoveState(NONE, 600); // interrupted before activation
		state.setMoveState(FORWARD, 700);
		assertEquals(NONE, state.getMoveState(1300)); // 600 ms of the new attempt
		assertEquals(FORWARD, state.getMoveState(1367)); // 667 ms of the new attempt
	}

	@Test
	void testActiveModifierFadesOutWhileMovingIntoAnotherDirection() {
		state.setMoveState(FORWARD, 0);
		state.setMoveState(SIDEWAYS, 1000); // forward was active and starts fading out
		assertEquals(FORWARD, state.getMoveState(1500)); // sideways still pending
		assertEquals(FORWARD | SIDEWAYS, state.getMoveState(1667)); // both apply
		assertEquals(SIDEWAYS, state.getMoveState(2332)); // forward faded out after 1332 ms
	}

	@Test
	void testAtMostTwoDirectionsApply() {
		state.setMoveState(FORWARD, 0);
		state.setMoveState(BACKWARD, 700); // forward fades out until 2032
		state.setMoveState(SIDEWAYS, 1400); // backward fades out until 2732, replacing forward as the confirmed one
		assertEquals(BACKWARD | SIDEWAYS, state.getMoveState(2100)); // forward is gone although its fade would still be running
		assertEquals(SIDEWAYS, state.getMoveState(2732));
	}

	@Test
	void testSwitchingBetweenLeftAndRightDoesNotRestartActivation() {
		// left and right map to the same value, so alternating between them is not a direction change
		for (long now = 0; now < 3000; now += 300)
			state.setMoveState(SIDEWAYS, now);
		assertEquals(SIDEWAYS, state.getMoveState(667));
	}

	@Test
	void testIntermittentMovementNeverActivates() {
		long now = 0;
		for (int i = 0; i < 10; i++) { // repeatedly moving forward and stopping must not activate anything, no matter for how long
			state.setMoveState(FORWARD, now);
			now += 600;
			state.setMoveState(NONE, now);
			now += 100;
			assertEquals(NONE, state.getMoveState(now));
		}
	}

	@Test
	void testResumedMovementRestartsActivationButKeepsTheFadeOut() {
		state.setMoveState(FORWARD, 0);
		state.setMoveState(NONE, 1000); // forward is active and fades out until 2332
		state.setMoveState(FORWARD, 1200); // resuming does not reactivate it, the pending timer starts over
		assertEquals(FORWARD, state.getMoveState(1500)); // still applied through the fade out
		assertEquals(FORWARD, state.getMoveState(2332)); // fade ended, but the new attempt has activated by now
		assertEquals(FORWARD, state.getMoveState(5000)); // still moving
	}

	@Test
	void testStopDoesNotDiscardFadingOutDirection() {
		state.setMoveState(FORWARD, 0);
		state.setMoveState(SIDEWAYS, 1000); // forward starts fading out, sideways is pending
		state.setMoveState(NONE, 1500); // sideways never activated and is discarded
		assertEquals(FORWARD, state.getMoveState(1500));
		assertEquals(FORWARD, state.getMoveState(2331));
		assertEquals(NONE, state.getMoveState(2332));
	}

	@Test
	void testRepeatedStopsKeepFadingOut() {
		state.setMoveState(FORWARD, 0);
		for (long now = 1000; now < 1050; now += 6) // the client sends several stop packets in a row, they must not cut the fade out short
			state.setMoveState(NONE, now);
		assertEquals(FORWARD, state.getMoveState(2331));
		assertEquals(NONE, state.getMoveState(2332));
	}

	@Test
	void testNoIdleTimeout() {
		// there is no safety net for missing packets: without a stop packet the direction simply stays applied
		state.setMoveState(FORWARD, 0);
		assertEquals(FORWARD, state.getMoveState(100_000));
	}
}
