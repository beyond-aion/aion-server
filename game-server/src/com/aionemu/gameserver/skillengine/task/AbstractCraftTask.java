package com.aionemu.gameserver.skillengine.task;

import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.gameobjects.player.Player;

/**
 * @author ATracer, synchro2
 */
public abstract class AbstractCraftTask extends AbstractInteractionTask {

	protected int completeValue = 100;
	protected int currentSuccessValue;
	protected int currentFailureValue;
	protected int skillLvlDiff;

	/**
	 * @param requestor
	 * @param responder
	 * @param successValue
	 * @param failureValue
	 */
	public AbstractCraftTask(Player requestor, VisibleObject responder, int skillLvlDiff) {
		super(requestor, responder);
		this.skillLvlDiff = skillLvlDiff;
	}

	@Override
	protected boolean onInteraction() {
		if (currentSuccessValue == completeValue) {
			return onSuccessFinish();
		}
		if (currentFailureValue == completeValue) {
			onFailureFinish();
			return true;
		}

		analyzeInteraction();

		sendInteractionUpdate();
		return false;
	}

	/**
	 * Perform interaction calculation
	 */
	protected abstract void analyzeInteraction();

	protected abstract void sendInteractionUpdate();

	protected abstract boolean onSuccessFinish();

	protected abstract void onFailureFinish();
}
