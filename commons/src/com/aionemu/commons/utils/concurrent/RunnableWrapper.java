package com.aionemu.commons.utils.concurrent;

/**
 * @author -Nemesiss-
 */
public class RunnableWrapper implements Runnable {

	private final Runnable runnable;
	private final long maxRuntimeMsWithoutWarning;

	public RunnableWrapper(Runnable runnable) {
		this(runnable, Long.MAX_VALUE);
	}

	public RunnableWrapper(Runnable runnable, long maxRuntimeMsWithoutWarning) {
		this.runnable = runnable;
		this.maxRuntimeMsWithoutWarning = maxRuntimeMsWithoutWarning;
	}

	@Override
	public final void run() {
		ExecuteWrapper.execute(runnable, maxRuntimeMsWithoutWarning);
	}
}
