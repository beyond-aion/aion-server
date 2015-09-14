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

	private final BitSet idList = new BitSet();
	private final ReentrantLock lock = new ReentrantLock();
	private AtomicInteger nextMinId = new AtomicInteger(1);
	protected static IdFactory instance = new IdFactory();

	public int nextId() {
		try {
			lock.lock();
			int id = idList.nextClearBit(nextMinId.intValue());
			idList.set(id);
			nextMinId.incrementAndGet();
			return id;
		} finally {
			lock.unlock();
		}
	}

	public static IdFactory getInstance() {
		return instance;
	}
}
