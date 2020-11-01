package com.aionemu.chatserver.utils;

import java.util.BitSet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Simplified version of idfactory
 * 
 * @author ATracer
 */
public class IdFactory {

	private static final IdFactory instance = new IdFactory();

	private final BitSet idList = new BitSet();
	private final ReentrantLock lock = new ReentrantLock();
	private final AtomicInteger nextMinId = new AtomicInteger(1);

	public static IdFactory getInstance() {
		return instance;
	}

	public int nextId() {
		try {
			lock.lock();
			int nextId = idList.nextClearBit(nextMinId.intValue());
			idList.set(nextId);
			nextMinId.incrementAndGet();
			return nextId;
		} finally {
			lock.unlock();
		}
	}
}
