package com.aionemu.gameserver.skillengine.task;

import java.util.concurrent.Future;

import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.utils.ThreadPoolManager;

/**
 * @author ATracer
 */
public abstract class AbstractInteractionTask {

	private Future<?> task;
	protected int interval = 2500;
	protected int delay = 1000;

	protected final Player requester;
	protected final VisibleObject responder;

	/**
	 * @param requester
	 * @param responder
	 */
	public AbstractInteractionTask(Player requester, VisibleObject responder) {
		this.requester = requester;
		if (responder == null)
			this.responder = requester;
		else
			this.responder = responder;
	}

	/**
	 * Called on each interaction
	 * 
	 * @return
	 */
	protected abstract boolean onInteraction();

	/**
	 * Called when interaction is complete
	 */
	protected abstract void onInteractionFinish();

	/**
	 * Called before interaction is started
	 */
	protected abstract void onInteractionStart();

	/**
	 * Called when interaction is not complete and need to be aborted
	 */
	protected abstract void onInteractionAbort();

	/**
	 * Interaction scheduling method
	 */
	public void start() {
		onInteractionStart();

		task = ThreadPoolManager.getInstance().scheduleAtFixedRate(new Runnable() {

			@Override
			public void run() {
				boolean stopTask = !requester.isOnline() || onInteraction();
				if (stopTask)
					stop();
			}

		}, delay, interval);
	}

	/**
	 * Stop current interaction
	 */
	public void stop() {
		onInteractionFinish();

		if (task != null && !task.isCancelled()) {
			task.cancel(false);
			task = null;
		}
	}

	/**
	 * Abort current interaction
	 */
	public void abort() {
		onInteractionAbort();
		stop();
	}

	/**
	 * @return true or false
	 */
	public boolean isInProgress() {
		return task != null && !task.isCancelled();
	}

	public void setInterval(int interval) {
		this.interval = interval;
	}
}
