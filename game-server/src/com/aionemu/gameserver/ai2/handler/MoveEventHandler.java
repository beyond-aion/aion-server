package com.aionemu.gameserver.ai2.handler;

import com.aionemu.gameserver.ai2.NpcAI2;

/**
 * @author ATracer
 */
public class MoveEventHandler {

	/**
	 * @param npcAI
	 */
	public static final void onMoveValidate(NpcAI2 npcAI) {
		if (npcAI.getOwner().canPerformMove()) {
			npcAI.getOwner().getController().onMove();
			TargetEventHandler.onTargetTooFar(npcAI);
		}
	}

	/**
	 * @param npcAI
	 */
	public static final void onMoveArrived(NpcAI2 npcAI) {
		npcAI.getOwner().getController().onMove();
		TargetEventHandler.onTargetReached(npcAI);
	}

}