package com.aionemu.commons.taskmanager;

import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @author lord_rex and MrPoke
 */
public abstract class AbstractLockManager {

	private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

	private final ReentrantReadWriteLock.WriteLock writeLock = lock.writeLock();
	private final ReentrantReadWriteLock.ReadLock readLock = lock.readLock(); // Later could be used.

	public final void writeLock() {
		writeLock.lock();
	}

	public final void writeUnlock() {
		writeLock.unlock();
	}

	public final void readLock() {
		readLock.lock();
	}

	public final void readUnlock() {
		readLock.unlock();
	}
}
