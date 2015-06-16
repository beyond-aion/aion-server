package com.aionemu.commons.utils.concurrent;

import java.util.concurrent.Delayed;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author NB4L1
 */
public final class ScheduledFutureWrapper implements ScheduledFuture<Object> {

	private final ScheduledFuture<?> future;

	public ScheduledFutureWrapper(ScheduledFuture<?> future) {
		this.future = future;
	}

	@Override
	public long getDelay(TimeUnit unit) {
		return future.getDelay(unit);
	}

	@Override
	public int compareTo(Delayed o) {
		return future.compareTo(o);
	}

	/**
	 * Just make sure to avoid wrong usage of Future.cancel(true).
	 */
	@Override
	public boolean cancel(boolean mayInterruptIfRunning) {
		return future.cancel(false);
	}

	@Override
	public Object get() throws InterruptedException, ExecutionException {
		return future.get();
	}

	@Override
	public Object get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
		return future.get(timeout, unit);
	}

	@Override
	public boolean isCancelled() {
		return future.isCancelled();
	}

	@Override
	public boolean isDone() {
		return future.isDone();
	}
}
