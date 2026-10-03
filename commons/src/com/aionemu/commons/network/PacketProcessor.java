package com.aionemu.commons.network;

import java.time.Duration;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.network.packet.BaseClientPacket;

/**
 * Packet Processor responsible for executing packets in correct order with respecting rules: - 1 packet / client at one time. - execute packets in
 * received order.<br>
 * Every connection keeps its own queue of pending packets, and connections with pending packets take turns, so a client flooding packets neither
 * slows down the selection of other clients' packets nor gets more than its share of the working threads.
 *
 * @author -Nemesiss-
 * @param <T>
 *          AConnection - owner of client packets.
 */
public class PacketProcessor<T extends AConnection<?>> {

	private static final Logger log = LoggerFactory.getLogger(PacketProcessor.class);

	/**
	 * When one working thread should be created.
	 */
	private final int threadSpawnThreshold;

	/**
	 * Max. pending packet count, where working threads can be killed (recover back to minThreads).
	 */
	private final int threadKillThreshold;

	/**
	 * Max. number of packets of a single connection that may wait for execution.
	 */
	private final int maxPendingPacketsPerConnection;

	private final Lock lock = new ReentrantLock();

	private final Condition notEmpty = lock.newCondition();

	/**
	 * Connections that have pending packets and none of them is being executed right now.
	 */
	private final Deque<AConnection<?>> readyConnections = new ArrayDeque<>();

	/**
	 * Number of packets of all connections waiting for execution.
	 */
	private int pendingPacketCount;

	/**
	 * Working threads.
	 */
	private final List<Thread> threads = new ArrayList<>();

	private final int minThreads;

	private final int maxThreads;

	private final Executor executor;

	private static class DummyExecutor implements Executor {

		@Override
		public void execute(Runnable command) {
			command.run();
		}
	}

	public PacketProcessor(int minThreads, int maxThreads, int threadSpawnThreshold, int threadKillThreshold, int maxPendingPacketsPerConnection) {
		this(minThreads, maxThreads, threadSpawnThreshold, threadKillThreshold, maxPendingPacketsPerConnection, new DummyExecutor());
	}

	public PacketProcessor(int minThreads, int maxThreads, int threadSpawnThreshold, int threadKillThreshold, int maxPendingPacketsPerConnection,
		Executor executor) {
		checkArgument(minThreads > 0, "Min Threads must be positive");
		checkArgument(maxThreads >= minThreads, "Max Threads must be >= Min Threads");
		checkArgument(threadSpawnThreshold > 0, "Thread Spawn Threshold must be positive");
		checkArgument(threadKillThreshold > 0, "Thread Kill Threshold must be positive");
		checkArgument(maxPendingPacketsPerConnection > 0, "Max pending packets per connection must be positive");

		this.minThreads = minThreads;
		this.maxThreads = maxThreads;
		this.threadSpawnThreshold = threadSpawnThreshold;
		this.threadKillThreshold = threadKillThreshold;
		this.maxPendingPacketsPerConnection = maxPendingPacketsPerConnection;
		this.executor = executor;

		if (minThreads != maxThreads)
			startCheckerThread();

		for (int i = 0; i < minThreads; i++)
			newThread();
	}

	private void checkArgument(boolean condition, String errorMessage) {
		if (!condition)
			throw new IllegalArgumentException(errorMessage);
	}

	private void startCheckerThread() {
		Thread.ofPlatform().name("PacketProcessor:Checker").start(new CheckerTask());
	}

	private boolean newThread() {
		if (threads.size() >= maxThreads)
			return false;

		String name = "PacketProcessor:" + threads.size();
		log.debug("Creating new PacketProcessor Thread: {}", name);

		Thread t = Thread.ofPlatform().name(name).unstarted(new PacketProcessorTask());
		threads.add(t);
		t.start();

		return true;
	}

	/**
	 * Kill one PacketProcessor Thread, but only if there are more working Threads than "minThreads"
	 */
	private void killThread() {
		if (threads.size() > minThreads) {
			Thread t = threads.removeLast();
			log.debug("Killing PacketProcessor Thread: {}", t.getName());
			t.interrupt();
		}
	}

	/**
	 * Queues the packet for execution after all previously received packets of its connection.
	 *
	 * @return False if the connection already has the maximum number of packets waiting for execution. The packet is dropped in that case and the
	 *         connection should be closed.
	 */
	public final boolean executePacket(BaseClientPacket<T> packet) {
		AConnection<?> connection = packet.getConnection();
		lock.lock();
		try {
			if (connection.pendingPackets.size() >= maxPendingPacketsPerConnection)
				return false;
			connection.pendingPackets.add(packet);
			pendingPacketCount++;
			if (!connection.scheduledForProcessing) {
				connection.scheduledForProcessing = true;
				readyConnections.add(connection);
				notEmpty.signal();
			}
			return true;
		} finally {
			lock.unlock();
		}
	}

	/**
	 * Takes the next packet of the connection that waited the longest. The connection stays scheduled until {@link #finishPacket} was called.
	 */
	private BaseClientPacket<? extends AConnection<?>> takeNextPacket() {
		while (readyConnections.isEmpty())
			notEmpty.awaitUninterruptibly();
		pendingPacketCount--;
		return readyConnections.poll().pendingPackets.poll();
	}

	private void finishPacket(AConnection<?> connection) {
		if (connection.pendingPackets.isEmpty()) {
			connection.scheduledForProcessing = false;
		} else {
			readyConnections.add(connection);
			notEmpty.signal();
		}
	}

	/**
	 * Packet Processor Task that will execute packet with respecting rules: - 1 packet / client at one time. - execute packets in received order.
	 * 
	 * @author -Nemesiss-
	 */
	private final class PacketProcessorTask implements Runnable {

		@Override
		public void run() {
			BaseClientPacket<? extends AConnection<?>> packet = null;
			for (;;) {
				lock.lock();
				try {
					if (packet != null)
						finishPacket(packet.getConnection());

					/* thread killed */
					if (Thread.interrupted())
						return;

					packet = takeNextPacket();
				} finally {
					lock.unlock();
				}
				executor.execute(packet);
			}
		}
	}

	/**
	 * Checking if PacketProcessor is busy or idle and increasing / decreasing numbers of threads.
	 */
	private final class CheckerTask implements Runnable {

		private static final Duration CHECK_INTERVAL = Duration.ofMinutes(1);
		private int previousPacketCount = 0;

		@Override
		public void run() {
			for (;;) {
				try {
					Thread.sleep(CHECK_INTERVAL);
				} catch (InterruptedException e) {
					return;
				}

				int packetsWaitingForExecution;
				lock.lock();
				try {
					packetsWaitingForExecution = pendingPacketCount;
				} finally {
					lock.unlock();
				}
				if (packetsWaitingForExecution <= previousPacketCount && packetsWaitingForExecution <= threadKillThreshold) {
					// reduce thread count by one
					killThread();
				} else if (packetsWaitingForExecution > threadSpawnThreshold) {
					// too small amount of threads
					if (!newThread() && packetsWaitingForExecution >= threadSpawnThreshold * 3)
						log.warn("Lag detected! [{} client packets are waiting for execution]. You should consider increasing PacketProcessor maxThreads or hardware upgrade.",
							packetsWaitingForExecution);
				}
				previousPacketCount = packetsWaitingForExecution;
			}
		}
	}
}
