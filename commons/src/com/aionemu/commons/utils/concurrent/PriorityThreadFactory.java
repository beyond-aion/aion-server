package com.aionemu.commons.utils.concurrent;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author -Nemesiss-
 */
public class PriorityThreadFactory implements ThreadFactory {

	/**
	 * Priority of new threads
	 */
	private final int prio;
	/**
	 * Thread group name
	 */
	private final String name;
	/**
	 * Number of created threads
	 */
	private final AtomicInteger threadNumber = new AtomicInteger(1);
	/**
	 * ThreadGroup for created threads
	 */
	private final ThreadGroup group;

	/**
	 * Constructor.
	 * 
	 * @param name
	 * @param prio
	 */
	public PriorityThreadFactory(String name, int prio) {
		this.prio = prio;
		this.name = name;
		this.group = new ThreadGroup(this.name);
	}

	@Override
	public Thread newThread(final Runnable r) {
		Thread t = new Thread(group, r);
		t.setName(name + "-" + threadNumber.getAndIncrement());
		t.setPriority(prio);
		return t;
	}
}
