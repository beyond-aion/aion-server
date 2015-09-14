package com.aionemu.gameserver.taskmanager;

import java.util.concurrent.locks.ReentrantLock;

import com.aionemu.gameserver.utils.ThreadPoolManager;

/**
 * @author NB4L1
 */
public abstract class FIFOExecutableQueue implements Runnable {

	private static final byte NONE = 0;
	private static final byte QUEUED = 1;
	private static final byte RUNNING = 2;

	private final ReentrantLock lock = new ReentrantLock();

	private volatile byte state = NONE;

	protected final void execute() {
		lock();
		try {
			if (state != NONE)
				return;

			state = QUEUED;
		} finally {
			unlock();
		}

		ThreadPoolManager.getInstance().execute(this);
	}

	public final void lock() {
		lock.lock();
	}

	public final void unlock() {
		lock.unlock();
	}

	@Override
	public final void run() {
		try {
			while (!isEmpty()) {
				setState(QUEUED, RUNNING);

				try {
					while (!isEmpty())
						removeAndExecuteFirst();
				} finally {
					setState(RUNNING, QUEUED);
				}
			}
		} finally {
			setState(QUEUED, NONE);
		}
	}

	private void setState(byte expected, byte value) {
		lock();
		try {
			if (state != expected)
				throw new IllegalStateException("state: " + state + ", expected: " + expected);
		} finally {
			state = value;

			unlock();
		}
	}

	protected abstract boolean isEmpty();

	protected abstract void removeAndExecuteFirst();
}
