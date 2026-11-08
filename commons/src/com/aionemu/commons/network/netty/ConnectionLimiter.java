package com.aionemu.commons.network.netty;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.Channel;

/**
 * Limits the connections of each IP address: how many may be open at the same time, and how fast new ones may be opened (a token bucket, which
 * keeps its state while the IP has no connection, so reconnecting doesn't reset it). A limit of all connections protects the memory from many IPs
 * at once. Rejected connections are closed before their pipeline is set up, so they cost no more than the accept.
 */
public class ConnectionLimiter {

	private static final Logger log = LoggerFactory.getLogger(ConnectionLimiter.class);
	private static final long REJECTION_LOG_INTERVAL_NANOS = TimeUnit.MINUTES.toNanos(1);
	private static final int PURGE_INTERVAL = 1024;

	private final int maxTotalConnections;
	private final AtomicInteger totalConnections = new AtomicInteger();
	private final AtomicLong lastTotalRejectionLogNanos = new AtomicLong(System.nanoTime() - REJECTION_LOG_INTERVAL_NANOS);
	private final int maxConnections;
	private final double connectionsPerNano;
	private final int burst;
	private final Map<InetAddress, Entry> entries = new ConcurrentHashMap<>();
	private int acceptsUntilPurge = PURGE_INTERVAL;

	private static class Entry {

		private int connections;
		private double tokens;
		private long lastRefillNanos;
		private long lastRejectionLogNanos;
		private int rejectedSinceLog;

		private Entry(int burst, long now) {
			tokens = burst;
			lastRefillNanos = now;
			lastRejectionLogNanos = now - REJECTION_LOG_INTERVAL_NANOS;
		}

		private void refill(long now, double connectionsPerNano, int burst) {
			tokens = Math.min(burst, tokens + (now - lastRefillNanos) * connectionsPerNano);
			lastRefillNanos = now;
		}
	}

	/**
	 * @param maxTotalConnections
	 *          Connections that may be open at the same time, 0 for no limit.
	 * @param maxConnections
	 *          Connections an IP may have open at the same time, 0 for no limit.
	 * @param connectionsPerSecond
	 *          Connections an IP may open per second on average, 0 for no limit.
	 * @param burst
	 *          Connections an IP may open at once before the rate applies.
	 */
	public ConnectionLimiter(int maxTotalConnections, int maxConnections, double connectionsPerSecond, int burst) {
		this.maxTotalConnections = maxTotalConnections;
		this.maxConnections = maxConnections;
		this.connectionsPerNano = connectionsPerSecond / TimeUnit.SECONDS.toNanos(1);
		this.burst = Math.max(1, burst);
	}

	/**
	 * @return An initializer that sets up accepted channels with the given one, or closes them if their IP exceeds a limit.
	 */
	public Consumer<Channel> wrap(Consumer<Channel> channelInitializer) {
		if (maxTotalConnections <= 0 && maxConnections <= 0 && connectionsPerNano <= 0)
			return channelInitializer;
		return channel -> {
			InetAddress ip = ((InetSocketAddress) channel.remoteAddress()).getAddress();
			if (tryAccept(ip, System.nanoTime())) {
				channel.closeFuture().addListener(_ -> release(ip));
				channelInitializer.accept(channel);
			} else {
				channel.close();
			}
		};
	}

	boolean tryAccept(InetAddress ip, long now) {
		if (maxTotalConnections > 0 && totalConnections.incrementAndGet() > maxTotalConnections) {
			totalConnections.decrementAndGet();
			long lastLog = lastTotalRejectionLogNanos.get();
			if (now - lastLog >= REJECTION_LOG_INTERVAL_NANOS && lastTotalRejectionLogNanos.compareAndSet(lastLog, now))
				log.warn("Rejected a connection from {}, {} connections are open (more are logged once a minute)", ip.getHostAddress(), maxTotalConnections);
			return false;
		}
		boolean[] accepted = new boolean[1];
		String[] rejection = new String[1];
		entries.compute(ip, (_, entry) -> {
			if (entry == null)
				entry = new Entry(burst, now);
			if (connectionsPerNano > 0)
				entry.refill(now, connectionsPerNano, burst);
			if (maxConnections > 0 && entry.connections >= maxConnections) {
				rejection[0] = entry.connections + " connections are open";
			} else if (connectionsPerNano > 0 && entry.tokens < 1) {
				rejection[0] = "it connects too fast";
			} else {
				entry.connections++;
				entry.tokens--;
				accepted[0] = true;
				return entry;
			}
			entry.rejectedSinceLog++;
			if (now - entry.lastRejectionLogNanos >= REJECTION_LOG_INTERVAL_NANOS) {
				log.warn("Rejected {} connection(s) from {} within the last minute, latest because {}", entry.rejectedSinceLog, ip.getHostAddress(),
					rejection[0]);
				entry.lastRejectionLogNanos = now;
				entry.rejectedSinceLog = 0;
			}
			return entry;
		});
		purgeIfDue(now);
		if (!accepted[0] && maxTotalConnections > 0)
			totalConnections.decrementAndGet();
		return accepted[0];
	}

	void release(InetAddress ip) {
		if (maxTotalConnections > 0)
			totalConnections.decrementAndGet();
		entries.computeIfPresent(ip, (_, entry) -> {
			entry.connections--;
			return entry;
		});
	}

	/**
	 * Removes the entries of IPs without connections whose rate limit has recovered, so they keep no memory.
	 */
	private void purgeIfDue(long now) {
		synchronized (entries) {
			if (--acceptsUntilPurge > 0)
				return;
			acceptsUntilPurge = PURGE_INTERVAL;
		}
		entries.keySet().forEach(ip -> entries.computeIfPresent(ip, (_, entry) -> {
			if (entry.connections > 0)
				return entry;
			if (connectionsPerNano > 0)
				entry.refill(now, connectionsPerNano, burst);
			return connectionsPerNano <= 0 || entry.tokens >= burst ? null : entry;
		}));
	}

	int size() {
		return entries.size();
	}
}
