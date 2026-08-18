package com.aionemu.gameserver.utils.stats;

import static com.aionemu.gameserver.controllers.movement.MovementModifierState.MoveState.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.aionemu.gameserver.model.stats.container.StatEnum;

/**
 * Pins the movement modifier table, so that a change to it is deliberate.
 */
class MovementModifierTableTest {

	private static void assertModifiers(int moveState, float attackFactor, float defenseFactor, int elementalDefense, int parry, int block,
										int dodge) {
		assertEquals(1000 * attackFactor, adjust(moveState, StatEnum.PHYSICAL_ATTACK), 0.01f);
		assertEquals(1000 * attackFactor, adjust(moveState, StatEnum.MAGICAL_ATTACK), 0.01f);
		assertEquals(1000 * defenseFactor, adjust(moveState, StatEnum.PHYSICAL_DEFENSE), 0.01f);
		assertEquals(1000 * defenseFactor, adjust(moveState, StatEnum.MAGICAL_DEFEND), 0.01f);
		assertEquals(1000 + elementalDefense, adjust(moveState, StatEnum.FIRE_RESISTANCE), 0.01f);
		assertEquals(1000 + parry, adjust(moveState, StatEnum.PARRY), 0.01f);
		assertEquals(1000 + block, adjust(moveState, StatEnum.BLOCK), 0.01f);
		assertEquals(1000 + dodge, adjust(moveState, StatEnum.EVASION), 0.01f);
	}

	private static float adjust(int moveState, StatEnum stat) {
		return StatFunctions.adjustStatByMoveState(moveState, stat, 1000);
	}

	@Test
	void testNotMoving() {
		assertModifiers(NONE, 1f, 1f, 0, 0, 0, 0);
	}

	@Test
	void testForward() {
		assertModifiers(FORWARD, 1.1f, 0.8f, -50, 0, 0, 0);
	}

	@Test
	void testBackward() {
		assertModifiers(BACKWARD, 0.8f, 1f, 0, 500, 500, 0);
	}

	@Test
	void testSideways() {
		assertModifiers(SIDEWAYS, 0.8f, 1f, 0, 0, 0, 300);
	}

	@Test
	void testForwardAndBackward() { // forward keeps its attack bonus but loses its defense penalties
		assertModifiers(FORWARD | BACKWARD, 1.1f, 1f, 0, 500, 500, 0);
	}

	@Test
	void testForwardAndSideways() {
		assertModifiers(FORWARD | SIDEWAYS, 1.1f, 1f, 0, 0, 0, 300);
	}

	@Test
	void testBackwardAndSideways() {
		assertModifiers(BACKWARD | SIDEWAYS, 0.8f, 1f, 0, 500, 500, 300);
	}

	@Test
	void testAllDirectionsCancelEachOther() {
		assertModifiers(FORWARD | BACKWARD | SIDEWAYS, 1f, 1f, 0, 0, 0, 0);
	}

	@Test
	void testUnaffectedStatsStayUnchanged() {
		assertEquals(1000, adjust(FORWARD, StatEnum.SPEED), 0.01f); // speed is not part of the table, see adjustSpeedByMovementModifier
		assertEquals(1000, adjust(FORWARD, StatEnum.PHYSICAL_CRITICAL), 0.01f);
		assertEquals(1000, adjust(BACKWARD, StatEnum.MAGICAL_CRITICAL), 0.01f);
	}
}
