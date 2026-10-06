package com.aionemu.gameserver.controllers.movement;

import static com.aionemu.gameserver.controllers.movement.PlayerMoveController.MoveRequest.*;
import static com.aionemu.gameserver.controllers.movement.PlayerMoveController.getMoveRequest;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/**
 * Tests which client move packets change the movement direction, for every mask the client sends.
 */
class MoveRequestTest {

	@Test
	void testMovementRequests() {
		assertEquals(DIRECTION, getMoveRequest((byte) 0xC0));
		assertEquals(DIRECTION, getMoveRequest((byte) 0xC4)); // gliding
		assertEquals(DIRECTION, getMoveRequest((byte) 0xC8)); // a new request while airborne still counts
	}

	@Test
	void testMovingToPointIsForward() {
		assertEquals(POINT, getMoveRequest((byte) 0xE0));
	}

	@Test
	void testUpdatesWithoutRequestKeepDirection() {
		assertEquals(CONTINUE, getMoveRequest((byte) 0x80));
		assertEquals(CONTINUE, getMoveRequest((byte) 0x84));
		assertEquals(CONTINUE, getMoveRequest((byte) 0xA0));
		assertEquals(CONTINUE, getMoveRequest((byte) 0x08));
		assertEquals(CONTINUE, getMoveRequest((byte) 0x48));
	}

	@Test
	void testStop() {
		assertEquals(STOP, getMoveRequest(MovementMask.IMMEDIATE));
		assertEquals(STOP, getMoveRequest((byte) 0x40));
	}
}
