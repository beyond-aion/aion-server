package com.aionemu.gameserver.ai.event;

import java.util.concurrent.LinkedBlockingDeque;

/**
 * @author ATracer
 */
public class AIEventLog extends LinkedBlockingDeque<AIEventType> {

	private static final long serialVersionUID = -7234174243343636729L;

	public AIEventLog() {
		super();
	}

	/**
	 * @param capacity
	 */
	public AIEventLog(int capacity) {
		super(capacity);
	}

	@Override
	public synchronized boolean offerFirst(AIEventType e) {
		if (remainingCapacity() == 0) {
			removeLast();
		}
		super.offerFirst(e);
		return true;
	}
}
