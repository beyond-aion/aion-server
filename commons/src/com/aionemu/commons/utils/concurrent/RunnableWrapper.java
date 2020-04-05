package com.aionemu.commons.utils.concurrent;

/**
 * @author -Nemesiss-
 */
public class RunnableWrapper implements Runnable {

	private final Runnable runnable;
	private final long maxRuntimeMsWithoutWarning;
	private final boolean catchAndLogThrowables;

	public RunnableWrapper(Runnable runnable) {
		this(runnable, Long.MAX_VALUE, true);
	}

	public RunnableWrapper(Runnable runnable, long maxRuntimeMsWithoutWarning, boolean catchAndLogThrowables) {
		this.runnable = runnable;
		this.maxRuntimeMsWithoutWarning = maxRuntimeMsWithoutWarning;
		this.catchAndLogThrowables = catchAndLogThrowables;
	}

	@Override
	public final void run() {
		ExecuteWrapper.execute(runnable, maxRuntimeMsWithoutWarning, catchAndLogThrowables);
	}
}
