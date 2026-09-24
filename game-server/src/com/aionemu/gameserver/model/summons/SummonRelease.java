package com.aionemu.gameserver.model.summons;

import java.util.concurrent.Future;

/**
 * Scheduled or already running release of a summon, see {@link com.aionemu.gameserver.model.gameobjects.Summon#registerRelease(SummonRelease)}.
 */
public class SummonRelease {

	private final UnsummonType unsummonType;
	private Future<?> task;
	private boolean started;

	public SummonRelease(UnsummonType unsummonType) {
		this.unsummonType = unsummonType;
	}

	public UnsummonType getUnsummonType() {
		return unsummonType;
	}

	public void setTask(Future<?> task) {
		this.task = task;
	}

	public void markStarted() {
		started = true;
	}

	public boolean hasStarted() {
		return started;
	}

	public boolean isCancelableByMaster() {
		return !started && unsummonType.isCancelableByMaster();
	}

	public boolean cancel() {
		if (started)
			return false;
		return task == null || task.cancel(false);
	}
}
