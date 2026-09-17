package com.aionemu.gameserver.skillengine.effect;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.aionemu.gameserver.skillengine.model.Effect;

/**
 * Pins the tick count of periodic effects: the duration is rounded down to a whole number of intervals plus one second, and ticking then runs while
 * more than one interval is left.
 */
class OverTimeTickCountTest {

	private static AbstractOverTimeEffect effectWithChecktime(int checktime) {
		AbstractOverTimeEffect effect = new AbstractOverTimeEffect() {

			@Override
			public void onPeriodicAction(Effect effect) {
			}
		};
		effect.checktime = checktime;
		return effect;
	}

	/** The number of ticks an effect with the given template duration ends up with, see {@link AbstractOverTimeEffect#startEffect}. */
	private static int tickCount(int duration, int checktime) {
		AbstractOverTimeEffect effect = effectWithChecktime(checktime);
		return effect.getTickCount((int) effect.roundDurationToTicks(duration));
	}

	@Test
	void testDurationIsRoundedDownToWholeTicksPlusOneSecond() {
		assertEquals(11000, effectWithChecktime(1000).roundDurationToTicks(10000));
		assertEquals(11000, effectWithChecktime(1000).roundDurationToTicks(10500)); // floored to 10000 first
		assertEquals(3000, effectWithChecktime(2000).roundDurationToTicks(3500)); // floored to 2000, so this one shrinks
		assertEquals(2000, effectWithChecktime(1000).roundDurationToTicks(1000));
	}

	@Test
	void testDurationIsUntouchedWithoutAnInterval() {
		assertEquals(10000, effectWithChecktime(0).roundDurationToTicks(10000));
		assertEquals(0, effectWithChecktime(1000).roundDurationToTicks(0));
		assertEquals(-5, effectWithChecktime(1000).roundDurationToTicks(-5));
	}

	@Test
	void testTickCounts() {
		assertEquals(1, tickCount(1000, 1000)); // Ripple of Purification: applied once, not twice
		assertEquals(2, tickCount(2000, 1000));
		assertEquals(10, tickCount(10000, 1000));
		assertEquals(5, tickCount(10000, 2000));
		assertEquals(21, tickCount(10000, 500));
		assertEquals(3, tickCount(9000, 3000));
	}

	@Test
	void testTickCountIgnoresTheRemainderOfAPartialInterval() {
		assertEquals(10, tickCount(10500, 1000)); // not 11
		assertEquals(1, tickCount(3500, 2000)); // not 2
	}

	@Test
	void testEffectsWithoutADurationOrIntervalDontTick() {
		assertEquals(0, effectWithChecktime(1000).getTickCount(0));
		assertEquals(0, effectWithChecktime(0).getTickCount(10000));
	}
}
