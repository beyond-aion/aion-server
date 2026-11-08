package com.aionemu.commons.network.netty;

import static org.junit.jupiter.api.Assertions.*;

import java.net.InetAddress;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;

public class ConnectionLimiterTest {

	private static final long SECOND = TimeUnit.SECONDS.toNanos(1);

	private static InetAddress ip(int lastByte) throws Exception {
		return InetAddress.getByAddress(new byte[] { 10, 0, 0, (byte) lastByte });
	}

	@Test
	public void limitsTheOpenConnectionsOfAnIp() throws Exception {
		ConnectionLimiter limiter = new ConnectionLimiter(2, 0, 1);
		assertTrue(limiter.tryAccept(ip(1), 0));
		assertTrue(limiter.tryAccept(ip(1), 0));
		assertFalse(limiter.tryAccept(ip(1), 0));
		assertTrue(limiter.tryAccept(ip(2), 0), "other IPs have their own limit");
		limiter.release(ip(1));
		assertTrue(limiter.tryAccept(ip(1), 0));
	}

	@Test
	public void limitsTheConnectionRateOfAnIpEvenIfItClosesItsConnections() throws Exception {
		ConnectionLimiter limiter = new ConnectionLimiter(0, 2, 3);
		long now = 5 * SECOND;
		for (int i = 0; i < 3; i++) {
			assertTrue(limiter.tryAccept(ip(1), now), "the burst");
			limiter.release(ip(1));
		}
		assertFalse(limiter.tryAccept(ip(1), now));
		assertFalse(limiter.tryAccept(ip(1), now + SECOND / 4));
		assertTrue(limiter.tryAccept(ip(1), now + SECOND / 2), "two connections per second");
		assertFalse(limiter.tryAccept(ip(1), now + SECOND / 2));
	}

	@Test
	public void forgetsIpsWithoutConnectionsOnceTheirRateRecovered() throws Exception {
		ConnectionLimiter limiter = new ConnectionLimiter(10, 1, 1);
		for (int i = 0; i < 1023; i++) { // the purge runs on every 1024th accept
			limiter.tryAccept(ip(i % 250 + 1), i * SECOND);
			limiter.release(ip(i % 250 + 1));
		}
		assertEquals(250, limiter.size());
		limiter.tryAccept(ip(251), 1023 * SECOND);
		assertEquals(1, limiter.size(), "only the IP with an open connection must be left");
	}
}
