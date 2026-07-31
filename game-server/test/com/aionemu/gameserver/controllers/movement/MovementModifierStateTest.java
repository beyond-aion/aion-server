package com.aionemu.gameserver.controllers.movement;

import static com.aionemu.gameserver.controllers.movement.MovementModifierState.Direction.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/**
 * Tests the retail movement modifier timings documented in https://github.com/beyond-aion/aion-server/issues/102
 */
class MovementModifierStateTest {

	private final MovementModifierState state = new MovementModifierState();

	@Test
	void testActivationDelay() {
		state.onMove(FORWARD, 0);
		assertEquals(MovementModifierDirection.NONE, state.getModifierDirection(0));
		assertEquals(MovementModifierDirection.NONE, state.getModifierDirection(849));
		assertEquals(MovementModifierDirection.FORWARD, state.getModifierDirection(850));
	}

	@Test
	void testModifierStaysActiveWhileMoving() {
		for (long now = 0; now <= 100_000; now += 500) // the client sends position updates about every 672ms while moving
			state.onMove(FORWARD, now);
		assertEquals(MovementModifierDirection.FORWARD, state.getModifierDirection(100_000));
	}

	@Test
	void testMovementEndsWithoutUpdates() {
		// we never learn about the end of the movement (e.g. connection loss), so it must not stay active forever
		state.onMove(FORWARD, 0);
		assertEquals(MovementModifierDirection.FORWARD, state.getModifierDirection(2000)); // movement assumed to have ended at 1000
		assertEquals(MovementModifierDirection.FORWARD, state.getModifierDirection(2349));
		assertEquals(MovementModifierDirection.NONE, state.getModifierDirection(2350));
	}

	@Test
	void testDeactivationDelay() {
		state.onMove(BACKWARD, 0);
		state.onStop(1000);
		assertEquals(MovementModifierDirection.BACKWARD, state.getModifierDirection(1000));
		assertEquals(MovementModifierDirection.BACKWARD, state.getModifierDirection(2349));
		assertEquals(MovementModifierDirection.NONE, state.getModifierDirection(2350));
	}

	@Test
	void testActivationProgressDoesNotAccumulate() {
		state.onMove(FORWARD, 0);
		state.onStop(700); // interrupted before activation
		assertEquals(MovementModifierDirection.NONE, state.getModifierDirection(700));
		state.onMove(FORWARD, 800);
		assertEquals(MovementModifierDirection.NONE, state.getModifierDirection(1450)); // 650 ms of the new attempt
		assertEquals(MovementModifierDirection.FORWARD, state.getModifierDirection(1650)); // 850 ms of the new attempt
	}

	@Test
	void testActiveModifierFadesOutWhileMovingIntoAnotherDirection() {
		state.onMove(FORWARD, 0);
		state.onMove(LEFT, 1000); // forward is active and starts fading out
		assertEquals(MovementModifierDirection.FORWARD, state.getModifierDirection(1800)); // forward active, sideways still pending
		assertEquals(MovementModifierDirection.FORWARD, state.getModifierDirection(2000)); // both active, forward has priority
		assertEquals(MovementModifierDirection.SIDEWAYS, state.getModifierDirection(2350)); // forward faded out after 1350 ms
	}

	@Test
	void testForwardHasPriorityOverAlreadyActiveSideways() {
		state.onMove(RIGHT, 0);
		state.onMove(FORWARD, 1000);
		assertEquals(MovementModifierDirection.SIDEWAYS, state.getModifierDirection(1500)); // forward still pending
		assertEquals(MovementModifierDirection.FORWARD, state.getModifierDirection(1850)); // forward activated, sideways still fading
	}

	@Test
	void testSidewaysHasPriorityOverBackward() {
		state.onMove(BACKWARD, 0);
		state.onMove(LEFT, 1000); // backward is active and starts fading out
		assertEquals(MovementModifierDirection.BACKWARD, state.getModifierDirection(1800)); // sideways still pending
		assertEquals(MovementModifierDirection.SIDEWAYS, state.getModifierDirection(1900)); // both active, sideways has priority
		assertEquals(MovementModifierDirection.SIDEWAYS, state.getModifierDirection(2400)); // backward faded out
	}

	@Test
	void testBackwardDoesNotOverrideActiveForward() {
		state.onMove(FORWARD, 0);
		state.onMove(BACKWARD, 1000);
		assertEquals(MovementModifierDirection.FORWARD, state.getModifierDirection(1900)); // both active, forward has priority
		assertEquals(MovementModifierDirection.BACKWARD, state.getModifierDirection(2350)); // forward faded out
	}

	@Test
	void testIntermittentMovementNeverActivates() {
		long now = 0;
		for (int i = 0; i < 10; i++) { // repeatedly moving forward and stopping must not activate anything, no matter for how long
			state.onMove(FORWARD, now);
			now += 600;
			state.onStop(now);
			now += 100;
			assertEquals(MovementModifierDirection.NONE, state.getModifierDirection(now));
		}
	}

	@Test
	void testActivationDelayIsNotResetByMovementContinuations() {
		// the client keeps sending movement packets without a destination (mask IMMEDIATE) while running, they must count as continued movement
		state.onMove(FORWARD, 0);
		state.onMove(FORWARD, 672);
		assertEquals(MovementModifierDirection.FORWARD, state.getModifierDirection(850));
	}

	@Test
	void testFrequentSidewaysSwitchingPreventsActivation() {
		long now = 0;
		for (int i = 0; i < 10; i++) {
			state.onMove(i % 2 == 0 ? LEFT : RIGHT, now);
			now += 500;
			assertEquals(MovementModifierDirection.NONE, state.getModifierDirection(now));
		}
	}

	@Test
	void testResumedMovementDoesNotCancelFadeOut() {
		state.onMove(FORWARD, 0);
		state.onStop(1000); // forward is active and fades out until 2350
		state.onMove(FORWARD, 1200); // resuming does not reactivate it, but the fade out continues
		assertEquals(MovementModifierDirection.FORWARD, state.getModifierDirection(2000));
		assertEquals(MovementModifierDirection.FORWARD, state.getModifierDirection(2050)); // reactivated after 850 ms of the new attempt
		state.onMove(FORWARD, 3000);
		assertEquals(MovementModifierDirection.FORWARD, state.getModifierDirection(3000)); // still moving, so it stays active beyond 2350
	}

	@Test
	void testStopDoesNotDiscardFadingOutDirections() {
		state.onMove(FORWARD, 0);
		state.onMove(LEFT, 1000); // forward starts fading out, sideways is pending
		state.onStop(1800);
		assertEquals(MovementModifierDirection.FORWARD, state.getModifierDirection(1800)); // sideways never activated, forward keeps fading out
		assertEquals(MovementModifierDirection.NONE, state.getModifierDirection(2350));
	}

	@Test
	void testRepeatedStopsKeepFadingOut() {
		state.onMove(FORWARD, 0);
		for (long now = 1000; now < 1050; now += 6) // the client sends several stop packets in a row, they must not cut the fade out short
			state.onStop(now);
		assertEquals(MovementModifierDirection.FORWARD, state.getModifierDirection(2349));
		assertEquals(MovementModifierDirection.NONE, state.getModifierDirection(2350));
	}

	@Test
	void testJumpActivatesDirectionOnLanding() {
		state.onMove(FORWARD, 0);
		state.commitCurrentDirection(300); // landed after a short jump forward
		assertEquals(MovementModifierDirection.FORWARD, state.getModifierDirection(300));
		state.onStop(400);
		assertEquals(MovementModifierDirection.FORWARD, state.getModifierDirection(1749)); // fades out normally
		assertEquals(MovementModifierDirection.NONE, state.getModifierDirection(1750));
	}

	@Test
	void testJumpFromStandstillActivatesOnLandingStopPacket() {
		// the landing packet is a stop packet, so the direction must be committed before the movement ends
		state.onMove(FORWARD, 0);
		state.commitCurrentDirection(748);
		state.onStop(748);
		assertEquals(MovementModifierDirection.FORWARD, state.getModifierDirection(748));
		assertEquals(MovementModifierDirection.FORWARD, state.getModifierDirection(2097));
		assertEquals(MovementModifierDirection.NONE, state.getModifierDirection(2098));
	}

	@Test
	void testJumpDoesNotShortenAlreadyActiveDirection() {
		state.onMove(FORWARD, 0);
		state.commitCurrentDirection(2000); // jumping while the bonus is already active must not affect it
		state.onStop(2500);
		assertEquals(MovementModifierDirection.FORWARD, state.getModifierDirection(3849));
		assertEquals(MovementModifierDirection.NONE, state.getModifierDirection(3850));
	}

	@Test
	void testUninterruptedTurningKeepsModifierActive() {
		state.onMove(FORWARD, 0);
		for (long now = 100; now < 5000; now += 100)
			state.onMove(FORWARD, now); // move packets while running straight ahead must not reset the timer
		assertEquals(MovementModifierDirection.FORWARD, state.getModifierDirection(5000));
	}
}
