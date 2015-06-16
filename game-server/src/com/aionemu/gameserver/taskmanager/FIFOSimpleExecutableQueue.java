package com.aionemu.gameserver.taskmanager;

import java.util.Collection;

import javolution.util.FastList;

/**
 * @author NB4L1
 */
public abstract class FIFOSimpleExecutableQueue<T> extends FIFOExecutableQueue {

	private final FastList<T> queue = new FastList<T>();

	public final void execute(T t) {
		synchronized (queue) {
			queue.addLast(t);
		}

		execute();
	}

	public final void executeAll(Collection<T> c) {
		synchronized (queue) {
			queue.addAll(c);
		}

		execute();
	}

	public final void remove(T t) {
		synchronized (queue) {
			queue.remove(t);
		}
	}

	@Override
	protected final boolean isEmpty() {
		synchronized (queue) {
			return queue.isEmpty();
		}
	}

	protected final T removeFirst() {
		synchronized (queue) {
			return queue.removeFirst();
		}
	}

	@Override
	protected abstract void removeAndExecuteFirst();
}
