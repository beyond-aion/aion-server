package com.aionemu.gameserver.services;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import org.junit.jupiter.api.Test;

import com.aionemu.gameserver.model.gameobjects.player.Player;

/**
 * Both clients can answer in the same moment, so two packet threads may check the partner's confirmation before either of them trades. Nothing
 * between that check and performTrade is atomic, and a duplicated trade moves items twice and releases object ids that are already in the other
 * inventory. The confirmation therefore has to stay serialized.
 */
class ExchangeServiceRaceTest {

	@Test
	void confirmExchangeIsSerialized() throws Exception {
		Method confirmExchange = ExchangeService.class.getDeclaredMethod("confirmExchange", Player.class);
		assertTrue(Modifier.isSynchronized(confirmExchange.getModifiers()),
			"confirmExchange must serialize both confirmations, otherwise the trade can run twice");
	}
}
