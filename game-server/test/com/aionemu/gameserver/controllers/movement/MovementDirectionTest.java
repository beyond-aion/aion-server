package com.aionemu.gameserver.controllers.movement;

import static com.aionemu.gameserver.controllers.movement.MovementModifierDirection.*;
import static com.aionemu.gameserver.controllers.movement.PlayableMoveController.calculateDirection;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/**
 * Tests the sector classification of the movement direction. Headings are client headings: 120 units of 3° each.
 */
class MovementDirectionTest {

	private static final byte UNITS_PER_TURN = 120;

	/**
	 * @param offset Units to the left (negative) or right (positive) of where the character looks.
	 */
	private static MovementModifierDirection directionAt(int ownHeading, int offset) {
		byte destinationHeading = (byte) (((ownHeading + offset) % UNITS_PER_TURN + UNITS_PER_TURN) % UNITS_PER_TURN);
		return calculateDirection(destinationHeading, (byte) ownHeading);
	}

	@Test
	void testStraightAhead() {
		for (int ownHeading = 0; ownHeading < UNITS_PER_TURN; ownHeading++)
			assertEquals(FORWARD, directionAt(ownHeading, 0));
	}

	@Test
	void testDiagonalsAreForwardAndSymmetric() {
		// W+D and W+A are 15 units off in either direction and must both count as forward, from every heading
		for (int ownHeading = 0; ownHeading < UNITS_PER_TURN; ownHeading++) {
			assertEquals(FORWARD, directionAt(ownHeading, 15), "right diagonal at heading " + ownHeading);
			assertEquals(FORWARD, directionAt(ownHeading, -15), "left diagonal at heading " + ownHeading);
		}
	}

	@Test
	void testSectorBoundaries() {
		assertEquals(FORWARD, directionAt(0, 15)); // 45°, still forward
		assertEquals(SIDEWAYS, directionAt(0, 16)); // 48°
		assertEquals(SIDEWAYS, directionAt(0, 44)); // 132°
		assertEquals(BACKWARD, directionAt(0, 45)); // 135°
		assertEquals(BACKWARD, directionAt(0, 60)); // 180°
	}

	@Test
	void testEveryOffsetIsMirrored() {
		for (int ownHeading = 0; ownHeading < UNITS_PER_TURN; ownHeading += 7) {
			for (int offset = 0; offset <= 60; offset++)
				assertEquals(directionAt(ownHeading, offset), directionAt(ownHeading, -offset),
					"asymmetric at heading " + ownHeading + ", offset " + offset);
		}
	}

	@Test
	void testStrafingIsSideways() {
		for (int ownHeading = 0; ownHeading < UNITS_PER_TURN; ownHeading++) {
			assertEquals(SIDEWAYS, directionAt(ownHeading, 30)); // 90°
			assertEquals(SIDEWAYS, directionAt(ownHeading, -30));
		}
	}

	@Test
	void testRunningBackwards() {
		for (int ownHeading = 0; ownHeading < UNITS_PER_TURN; ownHeading++)
			assertEquals(BACKWARD, directionAt(ownHeading, 60)); // 180°
	}
}
