package com.aionemu.gameserver.skillengine.task;

import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.gameobjects.player.Player;

/**
 * @author ATracer, synchro2
 */
public abstract class AbstractCraftTask extends AbstractInteractionTask {

	protected static final int fullBarValue = 1000;
	protected int currentSuccessValue;
	protected int currentFailureValue;
	protected int skillLvlDiff;
	protected CraftType craftType = CraftType.NORMAL;
	
	protected enum CraftType {
		NORMAL(1),
		CRIT_BLUE(2),
		CRIT_PURPLE(3);
		
		private int progressId;
		
		private CraftType(int progressId) {
			this.progressId = progressId;
		}
		
		public int getProgressId() {
			return progressId;
		}
	}
	
	/**
	 * @param requester
	 * @param responder
	 * @param successValue
	 * @param failureValue
	 */
	public AbstractCraftTask(Player requester, VisibleObject responder, int skillLvlDiff) {
		super(requester, responder);
		this.skillLvlDiff = skillLvlDiff;
	}

	@Override
	protected boolean onInteraction() {
		if (currentSuccessValue == fullBarValue) {
			return onSuccessFinish();
		}
		if (currentFailureValue == fullBarValue) {
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
