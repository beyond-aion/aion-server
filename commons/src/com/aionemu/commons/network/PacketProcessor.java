package com.aionemu.commons.network;

import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.Executor;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.network.packet.BaseClientPacket;

/**
 * Packet Processor responsible for executing packets in correct order with respecting rules: - 1 packet / client at one time. - execute packets in
 * received order.
 * 
 * @author -Nemesiss-
 * @param <T>
 *          AConnection - owner of client packets.
 */
public class PacketProcessor<T extends AConnection<?>> {

	/**
	 * Logger for PacketProcessor
	 */
	private static final Logger log = LoggerFactory.getLogger(PacketProcessor.class.getName());

	/**
	 * When one working thread should be created.
	 */
	private final int threadSpawnThreshold;

	/**
	 * Max. pending packet count, where working threads can be killed (recover back to minThreads).
	 */
	private final int threadKillThreshold;

	/**
	 * Lock for synchronization.
	 */
	private final Lock lock = new ReentrantLock();

	/**
	 * Not Empty condition.
	 */
	private final Condition notEmpty = lock.newCondition();

	/**
	 * Queue of packet that will be executed in correct order.
	 */
	private final List<BaseClientPacket<T>> packets = new LinkedList<>();

	/**
	 * Working threads.
	 */
	private final List<Thread> threads = new ArrayList<>();

	/**
	 * minimum number of working Threads
	 */
	private final int minThreads;

	/**
	 * maximum number of working Threads
	 */
	private final int maxThreads;

	/**
	 * Executor that will be used to execute packets
	 */
	private final Executor executor;

	private static class DummyExecutor implements Executor {

		@Override
		public void execute(Runnable command) {
			command.run();
		}
	}

	/**
	 * Create and start PacketProcessor responsible for executing packets.
	 * 
	 * @param minThreads
	 *          - minimum number of working Threads.
	 * @param maxThreads
	 *          - maximum number of working Threads.
	 * @param threadSpawnThreshold
	 *          - if not yet executed packets count exceeds given threshold then new thread would be spawned. (if current thread count is smaller than
	 *          maxThreads).
	 * @param threadKillThreshold
	 *          - if not yet executed packets count went below given threshold then one of worker thread will be killed (if current thread count is
	 *          bigger than minThreads).
	 */
	public PacketProcessor(int minThreads, int maxThreads, int threadSpawnThreshold, int threadKillThreshold) {
		this(minThreads, maxThreads, threadSpawnThreshold, threadKillThreshold, new DummyExecutor());
	}

	/**
	 * Create and start PacketProcessor responsible for executing packets.
	 * 
	 * @param minThreads
	 *          - minimum number of working Threads.
	 * @param maxThreads
	 *          - maximum number of working Threads.
	 * @param threadSpawnThreshold
	 *          - if not yet executed packets count exceeds given threshold then new thread would be spawned. (if current thread count is smaller than
	 *          maxThreads).
	 * @param threadKillThreshold
	 *          - if not yet executed packets count went below given threshold then one of worker thread will be killed (if current thread count is
	 *          bigger than minThreads).
	 * @param executor
	 *          - Executor that will be used to execute task (should be used only as decorator).
	 */
	public PacketProcessor(int minThreads, int maxThreads, int threadSpawnThreshold, int threadKillThreshold, Executor executor) {
		checkArgument(minThreads > 0, "Min Threads must be positive");
		checkArgument(maxThreads >= minThreads, "Max Threads must be >= Min Threads");
		checkArgument(threadSpawnThreshold > 0, "Thread Spawn Threshold must be positive");
		checkArgument(threadKillThreshold > 0, "Thread Kill Threshold must be positive");

		this.minThreads = minThreads;
		this.maxThreads = maxThreads;
		this.threadSpawnThreshold = threadSpawnThreshold;
		this.threadKillThreshold = threadKillThreshold;
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

	/**
	 * Start Checker Thread. Checker is responsible for increasing / reducing PacketProcessor Thread count based on Runtime needs.
	 */
	private void startCheckerThread() {
		Thread.ofPlatform().name("PacketProcessor:Checker").start(new CheckerTask());
	}

	/**
	 * Create and start new PacketProcessor Thread, but only if there wont be more working Threads than "maxThreads"
	 * 
	 * @return true if new Thread was created.
	 */
	private boolean newThread() {
		if (threads.size() >= maxThreads)
			return false;

		String name = "PacketProcessor:" + threads.size();
		log.debug("Creating new PacketProcessor Thread: " + name);

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
			log.debug("Killing PacketProcessor Thread: " + t.getName());
			t.interrupt();
		}
	}

	/**
	 * Add packet to execution queue and execute it as soon as possible on another Thread.
	 * 
	 * @param packet
	 *          that will be executed.
	 */
	public final void executePacket(BaseClientPacket<T> packet) {
		lock.lock();
		try {
			packets.add(packet);
			notEmpty.signal();
		} finally {
			lock.unlock();
		}
	}

	/**
	 * Return first packet available for execution with respecting rules: - 1 packet / client at one time. - execute packets in received order.
	 * 
	 * @return first available BaseClientPacket
	 */
	private BaseClientPacket<T> getFirstAvailable() {
		for (;;) {
			while (packets.isEmpty())
				notEmpty.awaitUninterruptibly();

			ListIterator<BaseClientPacket<T>> it = packets.listIterator();
			while (it.hasNext()) {
				BaseClientPacket<T> packet = it.next();
				if (packet.getConnection().tryLockConnection()) {
					it.remove();
					return packet;
				}
			}
			notEmpty.awaitUninterruptibly();
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
			BaseClientPacket<T> packet = null;
			for (;;) {
				lock.lock();
				try {
					if (packet != null)
						packet.getConnection().unlockConnection();

					/* thread killed */
					if (Thread.interrupted())
						return;

					packet = getFirstAvailable();
				} finally {
					lock.unlock();
				}
				executor.execute(packet);
			}
		}
	}

	/**
	 * Checking if PacketProcessor is busy or idle and increasing / reducing numbers of threads.
	 * 
	 * @author -Nemesiss-
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

				int packetsWaitingForExecution = packets.size();
				if (packetsWaitingForExecution <= previousPacketCount && packetsWaitingForExecution <= threadKillThreshold) {
					// reduce thread count by one
					killThread();
				} else if (packetsWaitingForExecution > threadSpawnThreshold) {
					// too small amount of threads
					if (!newThread() && packetsWaitingForExecution >= threadSpawnThreshold * 3)
						log.warn("Lag detected! [" + packetsWaitingForExecution
							+ " client packets are waiting for execution]. You should consider increasing PacketProcessor maxThreads or hardware upgrade.");
				}
				previousPacketCount = packetsWaitingForExecution;
			}
		}
	}
}
